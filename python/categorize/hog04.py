#importing required libraries
from sklearn.cluster import AgglomerativeClustering
from skimage.io import imread, imsave
from skimage.transform import resize
from skimage.feature import hog, local_binary_pattern
from skimage import exposure
from typing import Optional
from os import path, mkdir, link
import numpy as np
import typing
import csv
import os


def float_to_int(image):
    return (image * 255 / image.max()).astype('uint8')
    # points=16 => range=242
    # points=15 => range=212
    # points=14 => range=184
    # points=13 => range=158
    # points=12 => range=134
    # points=11 => range=112
    # points=10 => range=92
    # points=9  => range=74
    # points=8  => range=58
    # points 7  => range=44
    # points=6  => range=32
    # points=5  => range=22

lbp_ranges = [None, None, None, None, None, 22, 32, 44, 58, 74, 92, 112, 134, 158, 184, 212, 242]
affinities = ["euclidean", "cosine"]
methods = ["average", "ward", "complete", "single"]
color_name = ["Red", "Green", "Blue"]


def ensure_directory(next_dir_path):
    # print("Ensure directory: " + next_dir_path)
    if not path.isdir(next_dir_path):
        parent_path = path.dirname(next_dir_path)
        print("Parent is" + parent_path)
        ensure_directory(parent_path)
        mkdir(next_dir_path)
        if not path.isdir(next_dir_path):
            raise IOError("Could not create directory " + next_dir_path)



class LoadDensityModel:
    def __init__(self, image_dir, source_list, work_dir, specs):
        self.image_dir = image_dir + "/"
        self.source_list = source_list
        self.work_dir = work_dir
        self.points = [spec[0] for spec in specs]
        self.ranges = [lbp_ranges[spec[0]] for spec in specs]
        self.radii = [spec[1] for spec in specs]
        self.names = ["_P%d_R%d" % spec for spec in specs]
        ensure_directory(self.work_dir)
        with open(self.source_list, "r") as fd:
            self.source_images = [t.strip() for t in fd.readlines()]
        self.lane_list = path.join(
            self.work_dir, "lane_image_files.dat"
        )
        self.feature_files = [
            path.join(self.work_dir, "features_%s.csv" % (name))
            for name in self.names
        ]
        self.lane_images = None
        self.feature_vectors = None
        self.loaded_image_count = 0;
        self.loaded_vector_count = 0;


    def find_classes(self, expected_count):
        self.ensure_lanes()
        self.ensure_feature_vectors()

        class_range = range(0, expected_count + 1)
        cluster_dir = path.join(
            self.work_dir,
            "clusters_" + "_".join(self.names) + "-X%d" % (expected_count)
        )
        ensure_directory(cluster_dir)
        run_data = []
        for lane_idx in range(0, len(self.lane_images)):
            lane_vector = []
            for vector_index in range(0, len(self.feature_vectors)):
                print("Vector <%d> of lane <%d>" % (vector_index, lane_idx))
                lane_vector.extend(
                    self.feature_vectors[vector_index][lane_idx]
                )
            run_data.append(lane_vector)
        for affinity in affinities:
            affinity_dir = path.join(cluster_dir, affinity)
            ensure_directory(affinity_dir)
            for method in methods:
                if method == "ward" and affinity == "cosine":
                    continue
                method_dir = path.join(affinity_dir, method)
                ensure_directory(method_dir)
                for class_dir in class_range:
                    next_dir_path = path.join(method_dir, str(class_dir))
                    ensure_directory(next_dir_path)
        for method in methods:
            for affinity in affinities:
                if method == "ward" and affinity == "cosine":
                    continue
                method_dir = path.join(cluster_dir, affinity, method)
                print("Computing affinity=" + affinity + ", method=" + method)
                cluster = AgglomerativeClustering(
                    n_clusters=expected_count, affinity=affinity, linkage=method
                )
                # print(run_data)
                results = cluster.fit_predict(run_data)
                label_clusters = np.column_stack((self.lane_images, results))
                with open(path.join(method_dir, "results.csv"), "w") as file_out:
                    write_csv = csv.writer(
                        file_out, delimiter=",", lineterminator="\n", strict=1
                    )
                    for next_pair in label_clusters:
                        write_csv.writerow(next_pair)
                        src_path = next_pair[0]
                        dest_path = path.join(
                            method_dir,
                            str(next_pair[1]),
                            path.basename(src_path)
                        )
                        try:
                            link(src_path, dest_path)
                            print(src_path + " -> " + dest_path + "\n")
                        except FileExistsError:
                            print(src_path + " -> " + dest_path + " already exists\n")


    def ensure_lanes(self):
        if self.lane_images is None and path.exists(self.lane_list):
            with open(self.lane_list, "r") as lane_reader:
                loaded_pairs = [
                    line.split("::") for line in lane_reader.readlines()
                ]
                sources = np.array([pair[1] for pair in loaded_pairs])
                self.loaded_image_count = len(set(sources))
                self.lane_images = [pair[0] for pair in loaded_pairs]
        if self.loaded_image_count == len(self.source_images):
            return
        skip_count = self.loaded_image_count
        with open(self.lane_list, "a") as lane_writer:
            for source_path in self.source_images:
                if skip_count > 0:
                    skip_count = skip_count - 1
                    continue;
                relative_source = source_path.removeprefix(self.image_dir)
                minus_ext = path.splitext(relative_source)[0]
                print(self.image_dir)
                print(source_path)
                print(relative_source)
                print(self.work_dir)
                print("lane_images")

                lane_prefix = path.join(
                    self.work_dir, "lane_images", minus_ext
                )
                ensure_directory(path.dirname(lane_prefix))
                all_lane_image_data = self.find_lanes(
                    imread(source_path)
                )
                for lane_idx in range(0, 4):
                    lane_image_data = all_lane_image_data[lane_idx]
                    if lane_image_data is not None:
                        lane_file_path = "%s_lane%d.png" % (lane_prefix, lane_idx + 1)
                        print(lane_file_path)
                        imsave(lane_file_path, lane_image_data)
                        lane_writer.write("%s::%s\n" % (lane_file_path, source_path))
                        self.lane_images.append(lane_file_path)
                self.loaded_image_count = self.loaded_image_count + 1
        # This would de-dup the list but also reorder it!!
        # self.lane_images = list(set(self.lane_images))
        # Instead, presume all duplicates appear at the list end and truncate it
        # to the count of uniques
        self.loaded_image_count = len(set(self.lane_images))

    def ensure_feature_vectors(self):
        if self.feature_vectors is None:
            self.ensure_lanes()
            self.feature_vectors = []
            self.loaded_vector_count = []
            lane_image_count = len(set(self.lane_images))
            print("Lane Images Count is:")
            print(lane_image_count)
            for feature_index in range(0, len(self.feature_files)):
                feature_file = self.feature_files[feature_index]
                print(feature_file)
                vector_data = []
                vector_count = 0
                if path.exists(feature_file):
                     with open(feature_file, "r") as fileIn:
                         source = csv.reader(fileIn, delimiter="|")
                         vector_data = [[float(value) for value in row] for row in source]
                         vector_count = len(vector_data)
                self.feature_vectors.append(vector_data)
                self.loaded_vector_count.append(vector_count)
                print(vector_data)
                print(vector_count)
                print(lane_image_count)
                with open(feature_file, "a") as fileOut:
                    writer = csv.writer(
                        fileOut, delimiter="|", lineterminator="\n", strict=1
                    )
                    # while vector_count < lane_image_count:
                    skip_count = vector_count
                    for lane_image in self.lane_images:
                        if skip_count > 0:
                            skip_count = skip_count - 1
                            continue
                        image_data = imread(lane_image)
                        feature_data = self.describe(lane_image, image_data, feature_index)
                        writer.writerow(feature_data)
                        vector_data.append(feature_data)
                        vector_count += 1
                    self.feature_vectors.append(vector_data)
                    self.loaded_vector_count.append(vector_count)
                    print("End of Line")
                    print(len(vector_data))
                    print(vector_count)


    def find_lanes(self, img):
        bound_check = np.where(
            img[:,:,0:4] == (0, 0, 127, 255),
            np.zeros([1], dtype='uint16'),
            np.ones([1], dtype='uint16'),
        )[:, :, 0]
        row_bounds = bound_check.sum(axis=1)
        row_bound_indices = np.nonzero(row_bounds > 15)[0]
        if len(row_bound_indices) == 0:
            return [None, None, None, None]
        result = []
        row_min = row_bound_indices[0]
        row_max = row_bound_indices[-1] + 1
        print(row_min, row_max)
        all_col_bounds = bound_check.sum(axis=0)
        all_col_bounds = np.nonzero(all_col_bounds > 8)[0]
        col_approx = img.shape[1] / 4
        for col_index in range(0,4):
            col_min = col_index * col_approx
            col_max = col_min + col_approx
            col_bound_low = np.nonzero(col_min <= all_col_bounds)[0]
            col_bound_high = np.nonzero(all_col_bounds <= col_max)[0]
            if (len(col_bound_low) > 0) and (len(col_bound_high) > 0):
                col_min = all_col_bounds[col_bound_low[0]]
                col_max = all_col_bounds[col_bound_high[-1]] + 1
                print(col_min, col_max)
                if col_max > col_min:
                    result.append(
                        img[row_min:row_max,col_min:col_max,0:3]
                    )
                else:
                    result.append(None)
            else:
                result.append(None)
        return result
    
    
    # points=16 => range=242
    # points=15 => range=212
    # points=14 => range=184
    # points=13 => range=158
    # points=12 => range=134
    # points=11 => range=112
    # points=10 => range=92
    # points=9  => range=74
    # points=8  => range=58
    # points 7  => range=44
    # points=6  => range=32
    # points=5  => range=22
    # points=4  => range=14
    def describe(self, lane_file, lane_img, vector_index):
        points = self.points[vector_index]
        radius = self.radii[vector_index]
        lbp_range = self.ranges[vector_index]
        feature_vector = []
        for color_idx in range(0, 3):
            lbp = local_binary_pattern(
                lane_img[:,:,color_idx], points, radius, method="nri_uniform")
            (hist, _) = np.histogram(
                lbp.ravel(),
                bins=np.arange(0, lbp_range + 2),
                range=(0, lbp_range + 1)
            )
            # normalize the histogram
            hist = hist.astype("float")
            # hist = hist[1:-2]
            hist /= (hist.sum() + 1e-6)
            feature_vector.extend(hist)

            # Save the reference image
            lbp_file = lane_file.replace("lane_images", "local_patterns", 1)
            ensure_directory(
                path.dirname(lbp_file)
            )
            root_name = path.splitext(lbp_file)
            lbp_file = "%s-%s%s.png" % (root_name[0], color_name[color_idx], self.names[vector_index])
            imsave(lbp_file, (255 * (lbp / lbp_range)).astype('uint8'))
        return feature_vector
    


# img = "/Users/john.heinnickel/Git/JchPtf/ptf-app/python/categorize/grayscaled/clusters_120_4/euclidean/complete/2/105610_BeadDensity_1000.png"
# img = "/Users/john.heinnickel/Git/JchPtf/ptf-app/python/categorize/images/16/108624_BeadDensity_1000.png"
# img = "/Users/john.heinnickel/Git/JchPtf/ptf-app/python/categorize/images/9/115209_BeadDensity_1000.png"
# img = "/Users/john.heinnickel/Git/JchPtf/ptf-app/python/categorize/images/9/119849_BeadDensity_1000.png"
# img = "/Users/john.heinnickel/Git/JchPtf/ptf-app/python/categorize/images/9/115209_BeadDensity_1000.png"
# img = "/Users/john.heinnickel/Git/JchPtf/ptf-app/python/categorize/images/9/119433_BeadDensity_1000.png"
# img = "/Users/john.heinnickel/Git/JchPtf/ptf-app/python/categorize/images/9/119913_BeadDensity_1000.png"
# 
# img = imread(img)
# lanes = find_lanes(img)
# for lane_idx in range(1, 5):
#     if lanes[lane_idx - 1] is not None:
#         imsave('lane%d.png' % lane_idx, lanes[lane_idx - 1])
# 
# channel_names = ('R', 'G', 'B')
# pixels_per_cell = (8, 8)
# cells_per_block = (3, 3)
# # 9 for 3x3 cells, 12 for 4x4, and 20 for 5x5
# orientations = 15
# lanes_per_image = 4
# rescale_base = (
#     pixels_per_cell[0] * cells_per_block[0],
#     np.lcm(pixels_per_cell[1] * cells_per_block[1], lanes_per_image)
# )
# rescale_shape = (
#     rescale_base * (np.array(img.shape[0:2]) / rescale_base).round()
# ).astype('uint16').tolist()
# print(rescale_shape)
# 
# resized_img = float_to_int(resize(img[:,:,0:3], rescale_shape + [3]))
# print(resized_img.shape)
# imsave("resize02.png", resized_img)
# 
# print("Computing multichannel hog")
# mfd, m_hog_image = hog(
#     resized_img, orientations=orientations, block_norm='L2-Hys',
#     pixels_per_cell=pixels_per_cell, cells_per_block=cells_per_block,
#     visualize=True, multichannel=True, feature_vector=False
# )
# m_hog_image = float_to_int(m_hog_image)
# imsave("hog03_RGB.png", m_hog_image)
# print(mfd.shape)
# 
# c_fds = [None, None, None]
# c_hog_images = [None, None, None]
# for chan_idx in range(0, 3):
#     chan_img = resized_img[:,:,chan_idx] 
#     print("Computing %s channel hog" % channel_names[chan_idx])
#     cfd, c_hog_image = hog(
#         chan_img, orientations=orientations, block_norm='L2-Hys',
#         pixels_per_cell=pixels_per_cell, cells_per_block=cells_per_block,
#         visualize=True, multichannel=False, feature_vector=True
#     )
#     c_hog_images[chan_idx] = float_to_int(c_hog_image)
#     imsave("hog03_%s.png" % channel_names[chan_idx], c_hog_images[chan_idx])
#     c_fds[chan_idx] = cfd
#     print(cfd.shape)
# 
# img2 = np.array([
#     [c_hog_images[2], c_hog_images[0]],
#     [c_hog_images[1], m_hog_image]
# ])
# img2 = img2 \
#     .reshape([2, rescale_shape[0]*2, rescale_shape[1]]) \
#     .transpose([1,0,2]) \
#     .reshape([rescale_shape[0]*2, rescale_shape[1]*2])
# imsave("hog03.png", img2)
# print(img2.shape)
# print("Done02")
# 
