#importing required libraries
from skimage.io import imread, imsave
from skimage.transform import resize
from skimage.feature import hog
from skimage import exposure
import matplotlib.pyplot as plt
import numpy as np

img = "/Users/john.heinnickel/Git/JchPtf/ptf-app/python/categorize/grayscaled/clusters_120_4/euclidean/complete/2/105610_BeadDensity_1000.png"
img = "/Users/john.heinnickel/Git/JchPtf/ptf-app/python/categorize/images/16/108624_BeadDensity_1000.png"
img = imread(img)
print(img.shape)

pixels_per_cell = (4, 4)
cells_per_block = (3, 3)
lanes_per_image = 4
rescale_base = (
    pixels_per_cell[0] * cells_per_block[0],
    np.lcm(pixels_per_cell[1] * cells_per_block[1], lanes_per_image)
)
rescale_shape = (
    rescale_base * (np.array(img.shape[0:2]) / rescale_base).round()
).astype('uint16').tolist()
print(rescale_shape)

resized_img = resize(img, rescale_shape)
print(resized_img.shape)
imsave("resize.png", resized_img)

fd, hog_image = hog(
    resized_img, orientations=9, pixels_per_cell=pixels_per_cell,
    cells_per_block=cells_per_block, visualize=True, multichannel=True
)
plt.axis("off")
plt.imshow(hog_image, cmap="gray")
plt.imsave("hog01mp.png", hog_image, cmap="gray")
imsave("hog01.png", (hog_image * 255 / hog_image.max()).astype('uint8'))

print("Done01")
