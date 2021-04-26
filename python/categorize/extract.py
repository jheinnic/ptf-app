# import the necessary packages
from luminostics.categorize.pyimagesearch.localbinarypatterns import (
    LocalBinaryPatterns,
)
from imutils import paths
import argparse
import csv
import cv2

from luminostics.categorize.corpus.types import ImageCorpus


class ExtractLocalPatternsTask:
    def __init__(self, image_corpus: ImageCorpus):
# Initialize the local binary patterns descriptor along with data and label list.
# Include LBP instances for any additional outputs requested.
imageDir = args["images"]
labels = paths.list_images(imageDir)
dataSets = [
    LBPDataSet(
        output=args["output"], points=args["points"], radius=args["radius"]
    )
]
dataSets.extend(args["additionalDataSets"])
#
# Loop over images of input data set.  Convert each to a grayscale suitable for
# LBP analysis, then derive and append a row of data from each requested LBP specification.
for imagePath in labels:
    # load the image, convert it to grayscale, and describe it
    image = cv2.imread(imagePath)
    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    # extract the label from the image path, then update the
    # label and data lists
    # labels.append(imagePath.split("/")[-2])
    for dataSet in dataSets:
        dataSet.describe(gray)
#
# Flush and close each LBP specification's data file to cleanup before exiting.
for dataSet in dataSets:
    dataSet.close()

# # train a Linear SVM on the data
# model = LinearSVC(C=100.0, random_state=42)
# model.fit(data, labels)
#
# # loop over the testing images
# for imagePath in paths.list_images(args["testing"]):
# 	# load the image, convert it to grayscale, describe it,
# 	# and classify it
# 	image = cv2.imread(imagePath)
# 	gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
# 	hist = desc.describe(gray)
# 	prediction = model.predict(hist.reshape(1, -1))
#
# 	# display the image and the prediction
# 	cv2.putText(image, prediction[0], (10, 30), cv2.FONT_HERSHEY_SIMPLEX,
# 		1.0, (0, 0, 255), 3)
# 	cv2.imshow("Image", image)
# 	cv2.waitKey(0)
