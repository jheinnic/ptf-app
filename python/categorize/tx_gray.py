# import the necessary packages
from pyimagesearch.localbinarypatterns import (
    LocalBinaryPatterns, LBPDataSet
)
from skimage.io import imread, imsave
from skimage.color import rgb2gray
from os import path
import numpy as np
import argparse
import csv


# construct the argument parse to examine input arguments
ap = argparse.ArgumentParser()
ap.add_argument(
    "-f",
    "--imageList",
    dest="image_list",
    default="./images/collected_log.dat",
    type=str,
    help="file listing of each corpus image (default = './images/collected_log.dat')",
)
ap.add_argument(
    "-i",
    "--inputDir",
    dest="input_dir",
    default="images",
    type=str,
    help="directory to read RGB images from (default = './images')"
)
ap.add_argument(
    "-o",
    "--output_dir",
    dest="output_dir",
    default="grayscaled",
    type=str,
    help="directory to write grayscaled images to (default = './grayscaled')"
)
args = vars(ap.parse_args())
input_dir = path.abspath(args["input_dir"])
output_dir = path.abspath(args["output_dir"])
image_list = path.abspath(args["image_list"])
fd = open(image_list)
labels = [t.strip() for t in fd.readlines()]

# Loop over images of input data set.  Convert each to a grayscale suitable for
# LBP analysis, then derive and append a row of data from each requested LBP specification.
for image_path in labels:
    # load the image, convert it to grayscale, and describe it
    image = imread(image_path)
    image2 = np.array([image[:,:,0],image[:,:,2],image[:,:,1]])
    gray = image2.reshape([image.shape[0]*3, image.shape[1]])
    relative = image_path.removeprefix(input_dir + '/')
    write_path = path.join(output_dir, relative)
    print(write_path)
    imsave(write_path, gray)
