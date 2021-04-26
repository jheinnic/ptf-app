# import the necessary packages
import os

from skimage import feature
import numpy as np


class LBPType(object):
    def __init__(self, num_points, radius):
        self.num_points = num_points
        self.radius = radius


class LocalBinaryPatterns:
    def __init__(self, num_points, radius):
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


class LBPStore:
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
        images_dir = os.path.join(self.images_root, image_set_name)
        if not os.path.isdir(images_dir):
            raise ValueError(
                image_set_name
                + " is not an image set directory name under "
                + self.images_root
            )
        return LBPDataSet(images_dir, radius, points)


# Helper callable for extracting two numbers from a : separates string
class LBPDataSet:
    is_open: bool = False

    def __init__(self, images_dir, radius, points):
        self.is_open = False
        self.lbp = ""
        self.writer = ""
        self.data = ""
        try:
            self.output = kwargs["output"]
            self.points = kwargs["points"]
            self.radius = kwargs["radius"]
            self.open()
        except KeyError:
            self.output = ""
            self.points = ""
            self.radius = ""

    def __call__(self, param):
        tokens = param.split(":")
        self.output = open(tokens[0], "w")
        self.points = int(tokens[1])
        self.radius = int(tokens[2])
        self.open()
        return self

    def open(self):
        if not self.is_open:
            self.lbp = LocalBinaryPatterns(self.points, self.radius)
            self.writer = csv.writer(
                self.output, delimiter="|", lineterminator="\n", strict=1
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
            self.output.close()
            self.writer = ""
            self.lbp = ""
            self.data = ""
            self.output = ""
            self.points = ""
            self.radius = ""
            self.is_open = False
