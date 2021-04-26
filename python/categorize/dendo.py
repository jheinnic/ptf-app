# import scipy.cluster.hierarchy as shc
from scipy.cluster import hierarchy as sch  # dendrogram, linkage
from matplotlib import pyplot as plt
import csv
import random

#
data = []
with open('./images_24_8.csv', 'r') as fileIn:
    source = csv.reader(fileIn, delimiter="|")
    data.extend(source)
#
for row in data:
    rowLen = len(row)
    ii = 0
    while ii < rowLen:
        row[ii] = float(row[ii])
        ii += 1
#
rowCount = 800
labelList = range(1, rowCount)
viewCount = 2
methods = ['single', 'complete', 'average', 'weighted', 'centroid', 'median', 'ward']
while viewCount > 0:
    for method in methods:
        print(method);
        linked = sch.linkage(data[0:rowCount - 1], method=method)
        plt.figure(figsize=(18, 9))
        plt.title(method + " Dendogram")
        gram = sch.dendrogram(linked, orientation='top', labels=labelList, distance_sort='descending',
                              show_leaf_counts=True)
        plt.show()
    print(method);

    random.shuffle(data)
    viewCount -= 1
