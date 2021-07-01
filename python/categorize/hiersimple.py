import typing
from sklearn.cluster import AgglomerativeClustering
from os import mkdir, link, path
import numpy as np
import csv

rootDir = "grayscaled/clusters_120_4"

def ensureDirectory(nextDirPath):
    print("Ensure directory: " + nextDirPath)
    if not path.exists(nextDirPath):
        mkdir(nextDirPath)
        if not path.isdir(nextDirPath):
            raise IOError("Could not create directory " + nextDirPath)


affinities = ["euclidean", "cosine"]
methods = ["average", "ward", "complete", "single"]

expectedClusterCount = 52
classRange = range(0, expectedClusterCount + 1)
for affinity in affinities:
    affinityDir = path.join(rootDir, affinity)
    ensureDirectory(affinityDir)
    for method in methods:
        if method == "ward" and affinity == "cosine":
            continue
        methodDir = path.join(affinityDir, method)
        ensureDirectory(methodDir)
        for classDir in classRange:
            nextDirPath = path.join(methodDir, str(classDir))
            ensureDirectory(nextDirPath)

data: typing.List[typing.List[float]]
# with open("images_32_40.csv", "r") as fileIn:
with open("grayscaled/gsPatterns_120_4.csv", "r") as fileIn:
    source = csv.reader(fileIn, delimiter="|")
    data = [[float(value) for value in row] for row in source]

labels: typing.List[str]
with open("images/collected_log.dat", "r") as labelFileIn:
    labels = [label.strip() for label in labelFileIn]

if len(data) != len(labels):
    print(
        "WARNING: Row count ("
        + str(len(data))
        + ") does not match label count ("
        + str(len(labels))
        + ")"
    )

for method in methods:
    for affinity in affinities:
        if method == "ward" and affinity == "cosine":
            continue
        methodDir = path.join(rootDir, affinity, method)
        print("Computing affinity=" + affinity + ", method=" + method)
        cluster = AgglomerativeClustering(
            n_clusters=expectedClusterCount, affinity=affinity, linkage=method
        )
        results = cluster.fit_predict(data)
        labelClusters = np.column_stack((labels, results))
        with open(path.join(methodDir, "results.csv"), "w") as f:
            writeCsv = csv.writer(
                f, delimiter=",", lineterminator="\n", strict=1
            )
            for nextPair in labelClusters:
                srcPath = path.join("images", nextPair[0])
                destPath = path.join(methodDir, str(nextPair[1]), path.basename(nextPair[0]))
                link(srcPath, destPath)
                print(destPath + "\n")
                writeCsv.writerow(nextPair)
