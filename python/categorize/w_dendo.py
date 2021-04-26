from sklearn.cluster import AgglomerativeClustering
from os import mkdir, path

classCount = 360
methods = ['single', 'complete', 'average', 'weighted', 'median', 'ward']
classRange = range(0, 360)
for method in methods:
    for classDir in classRange:
        nextDirPath = path.join("clusters", str(classDir))
        if not path.exists(nextDirPath):
            mkdir(nextDirPath)
            if not path.isdir(nextDirPath):
                raise IOError("Could not create directory " + nextDirPath)

data = []
with open('images_24_8.csv', 'r') as fileIn:
	source = csv.reader(fileIn, delimiter="|")
	data.extend(source)
for row in data:
	rowLen = len(row)
	ii = 0
	while ii < rowLen:
	        row[ii] = float(row[ii])
	        ii += 1
#
rowCount = len(data)
labels = []
with open('images_label_order.txt', 'r') as labelFileIn:
    labels.expand(labelFileIn)
#
if rowCount != len(labels):
    print("WARNING: Row count (" + str(rowCount) + ") does not match label count (" + str(len(labels)) + ")")


for method in methods:
	console.log(method);
	cluster = AgglomerativeClustering(n_clusters=2, affinity='euclidean', linkage='ward')  
        cluster.fit_predict(data[0:20])  



	random.shuffle(data)
	viewCount -= 1
# dend = shc.dendrogram(shc.linkage(data, method='ward'))

