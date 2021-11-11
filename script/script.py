import json
import matplotlib.pyplot as plt
import numpy as np
import sys

with open(sys.argv[1]) as f:
    data = json.load(f)

# print(len(data['traces'][0]['events']))
# print([len(sent), len(received),len(datamove), len(parameters_set), len(metrics_update) ])
# total_time = received[-1]["time"]
# print(total_time)
# for data in data['traces'][0]['events']:
#   print(data)

# for sentP in sent:
#    print(sentP)
dico = {}
timeEvolution = []
for receivedP in data['traces'][0]["events"]:
    timeEvolution.append(float(receivedP['time']))
    if receivedP['name'] not in dico:
        dico[receivedP['name']] = [receivedP]
    else:
        dico[receivedP['name']].append(receivedP)

len_list = []
for keys in dico:
    len_list.append(len(dico[keys]))

print(timeEvolution)
plt.barh(list(dico.keys()), len_list)
plt.title("Number of different packets sent and received")
plt.xlabel("Number Of Packets")
plt.tight_layout()
plt.savefig('type_of_packet.png')
plt.close()
x = np.linspace(0, len(timeEvolution), len(timeEvolution))
plt.plot(x, timeEvolution)
text = plt.text(len(timeEvolution) + 10, timeEvolution[-1] - 2, "Total time taken = " + str(timeEvolution[-1]) + "ms\n"
                                                                                                                 "Total Number of packets = " + str(
    len(timeEvolution)))

plt.title("Time evolution (ms) with the packet number ")
plt.xlabel("Packet number")
plt.savefig('time_evolution.png', bbox_extra_artists=(text,), bbox_inches="tight")

