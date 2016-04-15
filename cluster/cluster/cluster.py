import sqlite3
from numpy import zeros, array
import numpy as np
from sklearn.cluster import DBSCAN
from sklearn import metrics
from sklearn.datasets.samples_generator import make_blobs
from sklearn.preprocessing import StandardScaler


db = sqlite3.connect('test.db')

c = db.cursor()
trajectories = []
for row in c.execute('SELECT * FROM TrajectorySchema'):
    trajectories.append(row)

N = int(len(trajectories)/2)
distance_matrix = zeros((N,N))
#temp = []
#for i in range(N):
#    temp.append(0)

#for i in range(N):
#    distance_matrix.append(temp.copy())
#    if(i % 100 == 0):
#       print("i = " + str(i))

#for i in range(N):
#    row = []
#    for j in range(N):
#        row.append(0)
#    distance_matrix.append(row)
#    if(i % 100 == 0):
#        print("i = " + str(i))

print("Finished Initializing the array")

for i in range(N):
    for j in range(N):
        trajA = trajectories[i]
        trajB = trajectories[j]
        distance_matrix[i, j] = str(i) + str(j)
        distance_matrix[j, i] = distance_matrix[i, j]
        #print(distance_matrix[i,j])

DBSCAN(ellipsis=3.0, min_samples=10)