from hog05bb import LoadDensityModel

model = LoadDensityModel(
    "/Users/john.heinnickel/Git/JchPtf/ptf-app/python/categorize/images",
    "/Users/john.heinnickel/Git/JchPtf/ptf-app/python/categorize/lane_model_best/best_src.dat",
    "/Users/john.heinnickel/Git/JchPtf/ptf-app/python/categorize/lane_model_best",
    ((5, 2), (7,6), (11,10))
)

model.find_classes(60)
