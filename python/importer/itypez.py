from enum import Enum


class WindowPoint(Enum):
    CENTER = "center"
    UPPER_LEFT = "upper_left"
    LOWER_RIGHT = "lower_right"


class PixelBasis(Enum):
    """
    When a given region is represented as finite series of pixels, its total extent in each dimension is divided into
    a finite number of sections.  For any value, N, where N describes the number of equally sizes regions the larger
    whole has been divided into, there are three possible ways to define what a pixel represents that yield a different
    mapping from object coordinates to image coordinates, although each is based on the same partitioning of object
    space into N equally sized regions.
    -- REGION_CENTER places N pixels, one at the center of each region.
    -- REGION_CORNER places N pixels, one at the same specific corner of each region.
    -- BETWEEN_REGIONS places N+1 pixels, one at the boundary between each region and one at each of the outer
       most perimeters.
    """
    CENTERS = "centers"
    CORNERS = "corners"
    BOUNDARIES = "boundaries"


class CalculationKind(Enum):
    NUMPY = "numpy"
    DICOM = "dicom"
