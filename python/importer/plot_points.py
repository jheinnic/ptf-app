import numpy as np

from itypez import PixelBasis, CalculationKind
from window_point import WindowPoint


PIXEL_COUNT = 480
DIMENSION_MIN = -1
DIMENSION_MAX = 1

PIXEL_WIDTH = 640
PIXEL_HEIGHT = 480
OUTPUT_SHAPE = [PIXEL_HEIGHT, PIXEL_WIDTH]

TOP = 1
BOTTOM = -1
LEFT = 1
RIGHT = -1


def check_resolution(pixel_height, pixel_width, pixel_unit):
    """
    Ensure that pixel_height x pixel_width can be evenly composed of pixels that are (pixel_unit x pixel_unit)
    in size.  For example, (480x640) resolution cannot be expressed with pixel sizes of either 12 or 64.  Although
    480 / 12 = 40 and 640 / 64 = 10, the other dimension fails to divide evenly, with 640/12 = 53.3333 and
    480 / 64 = 7.5.  But this resolution may use a pixel size of 20, because 480/20 = 24 and 640/20 = 32.
    """
    assert (pixel_height / pixel_unit) == (pixel_height // pixel_unit)
    assert (pixel_width / pixel_unit) == (pixel_width // pixel_unit)
    return True


def compute_frame_boundary(
    pixel_height: int, pixel_width: int,
    window_y: float, window_x: float,
    window_width: float, window_reference_point: WindowPoint
) -> (float, float, float, float):
    window_height = window_width * pixel_height / pixel_width
    if window_reference_point == WindowPoint.CENTER:
        bottom = window_y - (window_height / 2.0)
        right = window_x + (window_width / 2.0)
        left = window_x - (window_width / 2.0)
        top = window_y + (window_height / 2.0)
    elif window_reference_point == WindowPoint.LOWER_RIGHT:
        bottom = window_y
        right = window_x
        left = window_x - window_width
        top = window_y + window_height
    elif window_reference_point == WindowPoint.UPPER_LEFT:
        bottom = window_y - window_height
        right = window_x + window_width
        left = window_x
        top = window_y
    else:
        raise RuntimeError(f"Unrecognized window point enum value, {window_point}")
    return bottom, top, left, right


def _count_pixels(pixel_max, pixel_min=0, pixel_unit=1, dtype=np.uint16):
    return np.fromiter(
        range(pixel_min, pixel_max, pixel_unit),
        count=(pixel_max - pixel_min) // pixel_unit,
        dtype=dtype)


def compute_dicom_linear_dimensions(
    pixel_height: int, bottom: int, top: int,
    pixel_width: int, left: int, right: int,
    is_default: bool, pixel_unit: int = 1
):
    """
    See C.11.2.1 of http://dicom.nema.org/medical/dicom/current/output/html/part03.html#sect_5.3 for default
    See C.11.3.1 of http://dicom.nema.org/medical/dicom/current/output/html/part03.html#sect_5.3 for exact
    """
    range_height = top - bottom
    range_width = right - left
    height_divisor = (pixel_height - 1) if is_default else pixel_height
    width_divisor = (pixel_width - 1) if is_default else pixel_width
    pixel_heights = _count_pixels(pixel_height, pixel_unit=pixel_unit)
    pixel_lengths = _count_pixels(pixel_width, pixel_unit=pixel_unit)
    frame_heights = np.array([(x * range_height / height_divisor) + bottom for x in pixel_heights])
    frame_lengths = np.array([(x * range_width / width_divisor) + left for x in pixel_lengths])
    return pixel_heights, frame_heights, pixel_lengths, frame_lengths


def compute_sigmoid_dimensions(
    pixel_height: int, bottom: int, top: int, pixel_width: int, left: int, right: int, pixel_unit: int = 1
):
    pixel_mid_height = pixel_height / 2.0
    pixel_mid_width = pixel_width / 2.0
    range_height = top - bottom
    range_width = right - left
    pixel_heights = _count_pixels(pixel_height, pixel_unit=pixel_unit)
    pixel_lengths = _count_pixels(pixel_width, pixel_unit=pixel_unit)
    frame_heights = np.array([
        bottom + (range_height / (1 + np.exp(-8 * (x - pixel_mid_height) / pixel_height))) for x in pixel_heights])
    frame_lengths = np.array([
        left + (range_width / (1 + np.exp(-8 * (x - pixel_mid_width) / pixel_width))) for x in pixel_lengths])
    return pixel_heights, frame_heights, pixel_lengths, frame_lengths


def compute_numpy_linear_dimensions(
    pixel_height: int, bottom: int, top: int,
    pixel_width: int, left: int, right: int,
    is_default: bool, pixel_unit: int = 1
):
    frame_heights = np.linspace(bottom, top, pixel_height//pixel_unit, endpoint=is_default)
    frame_lengths = np.linspace(left, right, pixel_width//pixel_unit, endpoint=is_default)
    pixel_heights = _count_pixels(pixel_height, pixel_unit=pixel_unit)
    pixel_lengths = _count_pixels(pixel_width, pixel_unit=pixel_unit)
    return pixel_heights, frame_heights, pixel_lengths, frame_lengths


def plot_points(pixel_heights, frame_heights, pixel_lengths, frame_lengths):
    height_count = len(pixel_heights)
    length_count = len(pixel_lengths)
    assert height_count == len(frame_heights)
    assert length_count == len(frame_lengths)
    # Frame matrix
    frame_height_matrix = frame_heights.reshape([height_count, 1]).repeat(length_count, 1)
    frame_length_matrix = frame_lengths.reshape([1,length_count]).repeat(height_count, 0)
    frame_points = np.array([frame_height_matrix, frame_length_matrix]).transpose(1, 2, 0)
    # Pixel matrix
    pixel_height_matrix = pixel_heights.reshape([height_count, 1]).repeat(length_count, 1)
    pixel_length_matrix = pixel_lengths.reshape([1,length_count]).repeat(height_count, 0)
    pixel_points = np.array([pixel_height_matrix, pixel_length_matrix]).transpose(1, 2, 0)
    # TODO: Combine these two [x, y, 2] shaped matrices into a single [x, y, 4] matrix?
    return pixel_points, frame_points


def shift_to_center(pixel_heights, pixel_widths, top, right):
    """
    To shift an edge-based pixel sequence that has a one pixel-length gap between its last coordinate and its
    right-most or top-most point, this routine can be used to shift all values over by half a pixel length,
    moving their output values from the edge of each pixel to the center.  Do not use this with the DICOM
    default linear mapping, which compensates for the last pixel's extent by distributing it evenly across
    all the pixels, such that the left most pixels represent a point on hte left side of their pixel, and the
    right-most value represents a point on the right-most side, with pixels near the center mapping a spot
    midway between the pixel.  There is no such thing as shifting towards the center with DICOM default linear.
    """
    alt_heights = np.array([*pixel_heights[1:], top])
    alt_widths = np.array([*pixel_widths[1:], right])
    pixel_heights = (pixel_heights + alt_heights) / 2.0
    pixel_widths = (pixel_widths + alt_widths) / 2.0
    return pixel_heights, pixel_widths
