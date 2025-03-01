use std::os::raw::{c_char};
use std::ffi::{CString, CStr};
extern crate quiche;

extern crate log;

use std::net::ToSocketAddrs;

extern crate ring;
use ring::rand::*;

extern crate mio;
extern crate url;

const MAX_DATAGRAM_SIZE: usize = 1350;


pub fn run(domain_name: String) -> String { 

    let mut buf = [0; 65535];
    let mut out = [0; MAX_DATAGRAM_SIZE];
    let mut response_headers = String::from("");

    let url;
    // Domain name
    if domain_name == "https://linfo2142-grp4.info.ucl.ac.be" {
        url = url::Url::parse("https://130.104.229.81:443").unwrap(); // Ingi server
    }
    else {
        url = match url::Url::parse(&domain_name) {
            Err(_) => { return "[error] \n\nInvalid domain name".to_string(); },
            Ok(parsed_url) => parsed_url,
        };
    }

    // Setup the event loop.
    let poll = mio::Poll::new().unwrap();
    let mut events = mio::Events::with_capacity(1024);

    // Resolve server address.
    let peer_addr = match url.to_socket_addrs() {
        Err(_) => { return "[error] \n\nCannot resolve server address".to_string(); },
        Ok(mut peer_addr_result) => peer_addr_result.next().unwrap(),
    };

    // Bind to INADDR_ANY or IN6ADDR_ANY depending on the IP family of the
    // server address. This is needed on macOS and BSD variants that don't
    // support binding to IN6ADDR_ANY for both v4 and v6.
    let bind_addr = match peer_addr {
        std::net::SocketAddr::V4(_) => "0.0.0.0:0",
        std::net::SocketAddr::V6(_) => "[::]:0",
    };

    // Create the UDP socket backing the QUIC connection, and register it with
    // the event loop.
    let socket = std::net::UdpSocket::bind(bind_addr).unwrap();

    let socket = mio::net::UdpSocket::from_socket(socket).unwrap();
    poll.register(
        &socket,
        mio::Token(0),
        mio::Ready::readable(),
        mio::PollOpt::edge(),
    )
    .unwrap();

    // Create the configuration for the QUIC connection.
    let mut config = quiche::Config::new(quiche::PROTOCOL_VERSION).unwrap();

    config.verify_peer(false);

    config
        .set_application_protos(quiche::h3::APPLICATION_PROTOCOL)
        .unwrap();

    config.set_max_idle_timeout(5000);
    config.set_max_recv_udp_payload_size(MAX_DATAGRAM_SIZE);
    config.set_max_send_udp_payload_size(MAX_DATAGRAM_SIZE);
    config.set_initial_max_data(10_000_000);
    config.set_initial_max_stream_data_bidi_local(1_000_000);
    config.set_initial_max_stream_data_bidi_remote(1_000_000);
    config.set_initial_max_stream_data_uni(1_000_000);
    config.set_initial_max_streams_bidi(100);
    config.set_initial_max_streams_uni(100);
    config.set_disable_active_migration(true);

    let mut http3_conn = None;

    // Generate a random source connection ID for the connection.
    let mut scid = [0; quiche::MAX_CONN_ID_LEN];
    SystemRandom::new().fill(&mut scid[..]).unwrap();

    let scid = quiche::ConnectionId::from_ref(&scid);

    // Create a QUIC connection and initiate handshake.
    let mut conn =
        quiche::connect(url.domain(), &scid, peer_addr, &mut config).unwrap();

    let (write, send_info) = conn.send(&mut out).expect("initial send failed");

    while let Err(e) = socket.send_to(&out[..write], &send_info.to) {
        if e.kind() == std::io::ErrorKind::WouldBlock {
            continue;
        }

        return format!("send() failed: {:?}", e);
    }

    let h3_config = quiche::h3::Config::new().unwrap();

    // Prepare request.
    let mut path = String::from(url.path());

    if let Some(query) = url.query() {
        path.push('?');
        path.push_str(query);
    }

    let req = vec![
        quiche::h3::Header::new(b":method", b"GET"),
        quiche::h3::Header::new(b":scheme", url.scheme().as_bytes()),
        quiche::h3::Header::new(
            b":authority",
            url.host_str().unwrap().as_bytes(),
        ),
        quiche::h3::Header::new(b":path", path.as_bytes()),
        quiche::h3::Header::new(b"user-agent", b"quiche"),
    ];

    let mut req_sent = false;

    loop {
        poll.poll(&mut events, conn.timeout()).unwrap();

        // Read incoming UDP packets from the socket and feed them to quiche,
        // until there are no more packets to read.
        'read: loop {
            // If the event loop reported no events, it means that the timeout
            // has expired, so handle it without attempting to read packets. We
            // will then proceed with the send loop.
            if events.is_empty() {
                conn.on_timeout();
                break 'read;
            }

            let (len, from) = match socket.recv_from(&mut buf) {
                Ok(v) => v,

                Err(e) => {
                    // There are no more UDP packets to read, so end the read
                    // loop.
                    if e.kind() == std::io::ErrorKind::WouldBlock {
                        break 'read;
                    }
                    return format!("recv() failed: {:?}", e);
                },
            };

            let recv_info = quiche::RecvInfo { from };

            // Process potentially coalesced packets.
            let _read = match conn.recv(&mut buf[..len], recv_info) {
                Ok(v) => v,

                Err(_e) => {
                    continue 'read;
                },
            };
        }

        if conn.is_closed() {
            let ret = format!("{:?}", conn.stats());
            if conn.stats().recv == 0 {
                return format!("[error] \n\nrecv={}, sent={} \nDomain does not support QUIC or only partially", conn.stats().recv, conn.stats().sent);
            }
            return format!("[success] \n\nresponse headers: \n{} \nstats: \n{}", response_headers, ret);
        }

        // Create a new HTTP/3 connection once the QUIC connection is established.
        if conn.is_established() && http3_conn.is_none() {
            http3_conn = Some(
                quiche::h3::Connection::with_transport(&mut conn, &h3_config)
                    .unwrap(),
            );
        }

        // Send HTTP requests once the QUIC connection is established, and until
        // all requests have been sent.
        if let Some(h3_conn) = &mut http3_conn {
            if !req_sent {
                h3_conn.send_request(&mut conn, &req, true).unwrap();
                req_sent = true;
            }
        }

        if let Some(http3_conn) = &mut http3_conn {
            // Process HTTP/3 events.
            loop {
                match http3_conn.poll(&mut conn) {
                    Ok((_stream_id, quiche::h3::Event::Headers { list, .. })) => {
                        for header in list {
                            let name = quiche::h3::NameValue::name(&header);
                            let value = quiche::h3::NameValue::value(&header);
                            let mut concat_header = String::from("");
                            for c in name {
                                concat_header.push(*c as char);
                            }
                            concat_header.push_str(": ");
                            for c in value {
                                concat_header.push(*c as char);
                            }
                            concat_header.push_str("\n");
                            response_headers.push_str(&concat_header)
                        }
                    },

                    Ok((stream_id, quiche::h3::Event::Data)) => {
                        while let Ok(_read) =
                            http3_conn.recv_body(&mut conn, stream_id, &mut buf)
                        {

                        }
                    },

                    Ok((_stream_id, quiche::h3::Event::Finished)) => {
                        conn.close(true, 0x00, b"kthxbye").unwrap();
                    },

                    Ok((_stream_id, quiche::h3::Event::Reset(_e))) => {
                        conn.close(true, 0x00, b"kthxbye").unwrap();
                    },

                    Ok((_flow_id, quiche::h3::Event::Datagram)) => (),

                    Ok((_goaway_id, quiche::h3::Event::GoAway)) => {
                    },

                    Err(quiche::h3::Error::Done) => {
                        break;
                    },

                    Err(_e) => {
                        break;
                    },
                }
            }
        }

        // Generate outgoing QUIC packets and send them on the UDP socket, until
        // quiche reports that there are no more packets to be sent.
        loop {
            let (write, send_info) = match conn.send(&mut out) {
                Ok(v) => v,

                Err(quiche::Error::Done) => {
                    break;
                },

                Err(_e) => {
                    conn.close(false, 0x1, b"fail").ok();
                    break;
                },
            };

            if let Err(e) = socket.send_to(&out[..write], &send_info.to) {
                if e.kind() == std::io::ErrorKind::WouldBlock {
                    break;
                }
                return format!("send() failed: {:?}", e);
            }
        }

        if conn.is_closed() {
            let ret = format!("{:?}", conn.stats());
            if conn.stats().recv == 0 {
                return format!("[error] \n\nrecv={}, sent={} \nDomain does not support QUIC or only partially", conn.stats().recv, conn.stats().sent);
            }
            return format!("[success] \n\nresponse headers: \n{} \nstats: \n{}", response_headers, ret);
        }
    }

}


#[no_mangle]
pub extern fn quic_request(to: *const c_char) -> *mut c_char {
    let c_str = unsafe { CStr::from_ptr(to) };
    let domain_name = match c_str.to_str() {
        Err(_) => "localhost",
        Ok(string) => string,
    };

    let stats = run(domain_name.to_string());
    CString::new("Result: ".to_owned() + stats.as_str() + "\n\nDomain name entered: " + domain_name).unwrap().into_raw()
}


/// Expose the JNI interface for android below
#[cfg(target_os="android")]
#[allow(non_snake_case)]
pub mod android {
    extern crate jni;

    use super::*;
    use self::jni::JNIEnv;
    use self::jni::objects::{JClass, JString};
    use self::jni::sys::{jstring};

    #[no_mangle]
    pub unsafe extern fn Java_com_quicandroid_QuicRequest_quicRequest(env: JNIEnv, _: JClass, java_pattern: JString) -> jstring {
        let world = quic_request(env.get_string(java_pattern).expect("invalid pattern string").as_ptr());
        let world_ptr = CString::from_raw(world);
        let output = env.new_string(world_ptr.to_str().unwrap()).expect("Couldn't create java string!");
        output.into_inner()
    }
}