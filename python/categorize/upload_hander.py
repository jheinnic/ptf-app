faaaaaaaaarom libxmp import XMPFiles, consts, XMPMeta
from os import walk
from pathlib import Path

XMP_NS_RA_View_Spec  = 'http://ns.jchein.name/randart/0.1/view_spec'
VIEW_SPEC_PIXEL_HEIGHT = 'pixelHeight'
VIEW_SPEC_PIXEL_WIDTH = 'pixelWidth'
VIEW_SPEC_PIXEL_UNIT = 'pixelUnit'
VIEW_SPEC_BOTTOM = 'bottom'
VIEW_SPEC_TOP = 'top'
VIEW_SPEC_LEFT = 'left'
VIEW_SPEC_RIGHT = 'right'
VIEW_SPEC_POINT_MAP_URL = 'pointMapUrl'

XMP_NS_RA_Origin  = 'http://ns.jchein.name/randart/0.1/origin'
ORIGIN_PREFIX = 'prefix'
ORIGIN_SUFFIX = 'suffix'
ORIGIN_EXTENT = 'extent'
ORIGIN_ENTITY = 'entity'
ORIGIN_RELATION = 'relation'

XMP_NS_RA_Trace = 'http://ns.jchein.name/randart/0.1/trace'
TRACE_REQUESTED_AT = 'requestedAt'
TRACE_REQUEST_ID = 'requestId'
TRACE_PAINTER_NODE = 'painterNode'
TRACE_NODE_SEQUENCE = 'nodeSequence'
TRACE_PAINT_STARTED_AT = 'paintStartedAt'
TRACE_PAINT_ENDED_AT = 'paintEndedAt'
TRACE_INGESTED_AT = 'ingestedAt'

XMP_NS_RA_Track_Use = 'http://ns.jchein.name/randart/0.1/refs'
REFS_USED_BY = 'usedBy'

pixel_height = pixel_width = 400
top = right = 1
bottom = left = 0
pixel_unit = 1
point_map_url = 'http://view-specs.jchein.name/0.1/pointMaps/400-400,0-0,1-1/1'

extent = 'http://art-corpus.jchein.name/0.1/collections/1'

for parent, dirs, files in walk('./sample'):
    parent = Path(parent)
    for file in files:
        file_path='./' + str(parent/file)
        if not file_path.endswith('.png'):
            continue
        else:
            print(f"Processing {file_path}")
        xmpfile = XMPFiles(file_path=str(parent/file), open_forupdate=True)
        xmp = xmpfile.get_xmp()
        if xmp is None:
            xmp = XMPMeta()
        xmp.register_namespace(XMP_NS_RA_View_Spec, 'ravs')
        xmp.register_namespace(XMP_NS_RA_Origin, 'rao')
        # print(xmp.get_property_int(consts.XMP_NS_DC, 'fmt'))

        xmp.set_property_int(XMP_NS_RA_View_Spec, VIEW_SPEC_PIXEL_HEIGHT, pixel_height)
        xmp.set_property_int(XMP_NS_RA_View_Spec, VIEW_SPEC_PIXEL_WIDTH, pixel_width)
        xmp.set_property_int(XMP_NS_RA_View_Spec, VIEW_SPEC_PIXEL_UNIT, pixel_unit)
        xmp.set_property_int(XMP_NS_RA_View_Spec, VIEW_SPEC_TOP, top)
        xmp.set_property_int(XMP_NS_RA_View_Spec, VIEW_SPEC_BOTTOM, bottom)
        xmp.set_property_int(XMP_NS_RA_View_Spec, VIEW_SPEC_LEFT, left)
        xmp.set_property_int(XMP_NS_RA_View_Spec, VIEW_SPEC_RIGHT, right)
        xmp.set_property(XMP_NS_RA_View_Spec, VIEW_SPEC_POINT_MAP_URL, point_map_url)
        (prefix, suffix) = file[0:-4].replace('-', '_').split('_')[1:3]
        xmp.set_property(XMP_NS_RA_Origin, ORIGIN_PREFIX, prefix)
        xmp.set_property(XMP_NS_RA_Origin, ORIGIN_SUFFIX, suffix)
        xmp.set_property(XMP_NS_RA_Origin, ORIGIN_EXTENT, extent)

        if xmpfile.can_put_xmp(xmp):
            xmpfile.put_xmp(xmp)
        else:
            print(f"Could not put {xmp} to {file}")

        xmpfile.close_file()
