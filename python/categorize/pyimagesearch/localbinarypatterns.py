# import the necessary packages
import csv
import os
from typing import Optional

from skimage import feature
import numpy as np


class LocalBinaryPatterns:
    def __init__(self, num_points: int = 24, radius: int = 8):
        # store the number of points and radius
        self.num_points = num_points
        self.radius = radius

    def describe(self, image, eps=1e-7):
        # compute the Local Binary Pattern representation
        # of the image, and then use the LBP representation
        # to build the histogram of patterns
        lbp = feature.local_binary_pattern(
            image, self.num_points, self.radius, method="uniform"
        )
        (hist, _) = np.histogram(
            lbp.ravel(),
            bins=np.arange(0, self.num_points + 3),
            range=(0, self.num_points + 2),
        )

        # normalize the histogram
        hist = hist.astype("float")
        hist /= hist.sum() + eps

        # return the histogram of Local Binary Patterns
        return hist


class LBPDataDir:
    def __init__(self, data_root, images_root):
        self.data_root = data_root
        self.images_root = images_root
        if not os.path.exists(data_root):
            try:
                os.makedirs(data_root)
            except (FileNotFoundError, OSError):
                self.is_valid = False
        self.is_valid = os.path.isdir(data_root) and os.path.isdir(images_root)

    def access(self, image_set_name, radius, points):
        if image_set_name.find("_") > -1:
            raise ValueError('Do not use "_" in image set directory names')
        if os.path.isdir():
            raise ValueError("directory does not exist")


# Helper callable for extracting two numbers from a : separates string
class LBPDataSet:
    lbp: Optional[LocalBinaryPatterns] = None

    def __init__(self, output: str, points: int = None, radius: int = None):
        self.is_open = False
        self.lbp = None
        self.writer = ""
        self.data = ""
        try:
            self.output = output
            self.points = points
            self.radius = radius
            self.open()
        except KeyError:
            self.output = ""
            self.points = ""
            self.radius = ""

    def __call__(self, param):
        tokens = param.split(":")
        self.openFile = None
        self.output = tokens[0] # open(tokens[0], "w")
        self.points = int(tokens[1])
        self.radius = int(tokens[2])
        self.open()
        return self

    def open(self):
        if not self.is_open:
            self.openFile = open(self.output, "w")
            self.lbp = LocalBinaryPatterns(self.points, self.radius)
            self.writer = csv.writer(
                self.openFile, delimiter="|", lineterminator="\n", strict=1
            )
            self.data = []
            self.is_open = True

    def describe(self, gray):
        if self.is_open:
            histogram = self.lbp.describe(gray)
            self.data.append(histogram)
            self.writer.writerow(histogram)

    def close(self):
        if self.is_open:
            self.openFile.close()
            self.writer = ""
            self.lbp = ""
            self.data = ""
            self.output = ""
            self.points = ""
            self.radius = ""
            self.is_open = False
