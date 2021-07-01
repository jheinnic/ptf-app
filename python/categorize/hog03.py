#importing required libraries
from skimage.io import imread, imsave
from skimage.transform import resize
from skimage.feature import hog
from skimage import exposure
import matplotlib.pyplot as plt
import numpy as np

def float_to_int(image):
    return (image * 255 / image.max()).astype('uint8')

img = "/Users/john.heinnickel/Git/JchPtf/ptf-app/python/categorize/grayscaled/clusters_120_4/euclidean/complete/2/105610_BeadDensity_1000.png"
img = "/Users/john.heinnickel/Git/JchPtf/ptf-app/python/categorize/images/16/108624_BeadDensity_1000.png"
img = "/Users/john.heinnickel/Git/JchPtf/ptf-app/python/categorize/images/9/115209_BeadDensity_1000.png"
img = "/Users/john.heinnickel/Git/JchPtf/ptf-app/python/categorize/images/9/119849_BeadDensity_1000.png"
img = "/Users/john.heinnickel/Git/JchPtf/ptf-app/python/categorize/images/9/115209_BeadDensity_1000.png"
img = "/Users/john.heinnickel/Git/JchPtf/ptf-app/python/categorize/images/9/119433_BeadDensity_1000.png"
img = "/Users/john.heinnickel/Git/JchPtf/ptf-app/python/categorize/images/9/119913_BeadDensity_1000.png"

img = imread(img)
bg = (img != (0, 0, 127, 255))
img = img * bg
print(img.shape)

channel_names = ('R', 'G', 'B')
pixels_per_cell = (8, 8)
cells_per_block = (3, 3)
# 9 for 3x3 cells, 12 for 4x4, and 20 for 5x5
orientations = 15
lanes_per_image = 4
rescale_base = (
    pixels_per_cell[0] * cells_per_block[0],
    np.lcm(pixels_per_cell[1] * cells_per_block[1], lanes_per_image)
)
rescale_shape = (
    rescale_base * (np.array(img.shape[0:2]) / rescale_base).round()
).astype('uint16').tolist()
print(rescale_shape)

resized_img = float_to_int(resize(img[:,:,0:3], rescale_shape + [3]))
print(resized_img.shape)
imsave("resize02.png", resized_img)

print("Computing multichannel hog")
mfd, m_hog_image = hog(
    resized_img, orientations=orientations, block_norm='L2-Hys',
    pixels_per_cell=pixels_per_cell, cells_per_block=cells_per_block,
    visualize=True, multichannel=True, feature_vector=False
)
m_hog_image = float_to_int(m_hog_image)
imsave("hog03_RGB.png", m_hog_image)
print(mfd.shape)

c_fds = [None, None, None]
c_hog_images = [None, None, None]
for chan_idx in range(0, 3):
    chan_img = resized_img[:,:,chan_idx] 
    print("Computing %s channel hog" % channel_names[chan_idx])
    cfd, c_hog_image = hog(
        chan_img, orientations=orientations, block_norm='L2-Hys',
        pixels_per_cell=pixels_per_cell, cells_per_block=cells_per_block,
        visualize=True, multichannel=False, feature_vector=True
    )
    c_hog_images[chan_idx] = float_to_int(c_hog_image)
    imsave("hog03_%s.png" % channel_names[chan_idx], c_hog_images[chan_idx])
    c_fds[chan_idx] = cfd
    print(cfd.shape)

img2 = np.array([
    [c_hog_images[2], c_hog_images[0]],
    [c_hog_images[1], m_hog_image]
])
img2 = img2 \
    .reshape([2, rescale_shape[0]*2, rescale_shape[1]]) \
    .transpose([1,0,2]) \
    .reshape([rescale_shape[0]*2, rescale_shape[1]*2])
imsave("hog03.png", img2)
print(img2.shape)
print("Done02")

