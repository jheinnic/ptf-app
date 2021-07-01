from hog04 import LoadDensityModel

model = LoadDensityModel(
    "/Users/john.heinnickel/Git/JchPtf/ptf-app/python/categorize/images",
    "/Users/john.heinnickel/Git/JchPtf/ptf-app/python/categorize/lane_model_test/test_src.dat",
    "/Users/john.heinnickel/Git/JchPtf/ptf-app/python/categorize/lane_model_test",
    ((5, 2), (7,6), (11,10))
)

model.find_classes(60)
