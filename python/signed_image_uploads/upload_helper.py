# Cloudinary settings using python code. Run before pycloudinary is used.
from datetime import datetime
import typing

import cloudinary
import dotenv

from signed_image_uploads_pb2 import CreateSingleSignedUpload, BeginSignedUploadSeries, ContinueSignedUploadSeries, \
    OptionalUploadFeatureKind, SignedUploadRequestResult
from typez import IPublicIdHelper, IUploadHelper

dotenv.load_dot_env()
_cloudinary_api_key = os.environ['CLOUDINARY_API_KEY']
_cloudinary_api_secret = os.environ['CLOUDINARY_API_SECRET']
cloudinary.config(
    cloud_name=os.environ['CLOUDINARY_CLOUD_NAME'],
    api_key=_cloudinary_api_key,
    api_secret=_cloudinary_api_secret
)

# cloudinary.utils.api_sign_request(
#     dict(public_id="sample", version="1312461204"), "my_api_secret")
# public_id = "sample"
# version = "1315060510"
# secret = "abcd"
# to_sign = 'public_id=' + public_id + '&' + 'version=' + version
# signature = Base64.urlsafe_encode64(Digest::SHA1.digest(to_sign + secret))

request = dict(
    public_id="foo",
    format="png",
    phash=True,
    colors=True,
    image_metadata=True,
    quality_analysis=True
)
signature = cloudinary.utils.api_sign_request(request, _cloudinary_api_secret)
request["signature"] = signature
request["api_key"] = _cloudinary_api_key
params = cloudinary.utils.build_upload_params(**request)
print(signature)
print(cloudinary.utils.urlencode(request))


class UploadHelper(IUploadHelper):
    def __init__(self, public_id_helper: IPublicIdHelper):
        self._cloudinary = cloudinary
        self._public_id_helper = public_id_helper

    def sign_upload_request(self, request: CreateSingleSignedUpload) -> SignedUploadRequestResult:
        _public_id = self._public_id_helper.get_new_id()
        return self._parse_features(_public_id, request)

    def sign_first_upload_request(self, request: BeginSignedUploadSeries) -> SignedUploadRequestResult:
        _public_id = self._public_id_helper.get_new_id(is_series=True)
        return self._parse_features(_public_id, request)

    def sign_next_upload_request(self, request: ContinueSignedUploadSeries) -> SignedUploadRequestResult:
        _public_id = self._public_id_helper.get_next_id(request.latestPublicId)
        return self._parse_features(_public_id, request)

    def _parse_features(
        self,
        _public_id: str,
        _proto_msg: typing.Union[CreateSingleSignedUpload, BeginSignedUploadSeries, ContinueSignedUploadSeries]
    ) -> str:
        _request_dict = dict(
            public_id=_public_id,
            format="png",
            phash=True
        )
        tag_vendors: typing.Optional[str] = None
        for feature in _proto_msg.activeFeatures:
            if feature == OptionalUploadFeatureKind.COLOR_ANALYSIS:
                _request_dict["colors"] = True
            elif feature == OptionalUploadFeatureKind.QUALITY_ANALYSIS:
                _request_dict["quality_analysis"] = True
            elif feature == OptionalUploadFeatureKind.IMAGE_METADATA:
                _request_dict["image_metadata"] = True
            elif tag_vendors is None:
                if feature == OptionalUploadFeatureKind.GOOGLE_TAGGING:
                    tag_vendors = "google_tagging"
                elif feature == OptionalUploadFeatureKind.IMAGGA_TAGGING:
                    tag_vendors = "imagga_tagging"
                elif feature == OptionalUploadFeatureKind.AWS_REK_TAGGING:
                    tag_vendors = "aws_rek_tagging"
                else:
                    raise RuntimeError(f"Don't know how to request image tagging from {feature}")
            elif feature == OptionalUploadFeatureKind.GOOGLE_TAGGING:
                tag_vendors = tag_vendors + ",google_tagging"
            elif feature == OptionalUploadFeatureKind.IMAGGA_TAGGING:
                tag_vendors = tag_vendors + ",imagga_tagging"
            elif feature == OptionalUploadFeatureKind.AWS_REK_TAGGING:
                tag_vendors = tag_vendors + ",aws_rek_tagging"
            else:
                raise RuntimeError(f"Don't know how to request image tagging from {feature}")
        if tag_vendors is not None:
            _request_dict["categorization"] = tag_vendors
            if _proto_msg.autoTagThreshold is not None and _proto_msg.autoTagThreshold > 0:
                _request_dict["auto_tagging"] = _proto_msg.autoTagThreshold
        now_time = datetime.now().timestamp()
        _request_dict["timestamp"] = now_time
        _signature = cloudinary.utils.api_sign_request(_request_dict, _cloudinary_api_secret)
        _request_dict["signature"] = _signature
        _request_dict["api_key"] = _cloudinary_api_key
        _request_dict["resource_type"] = "image"
        _query_string = cloudinary.utils.urlencode(_request_dict)
        _upload_url = cloudinary.utils.cloudinary_api_url()
        print(f"Calculated {_signature} for {_query_string} and {_upload_url} from {_request_dict}")

        return_value = SignedUploadRequestResult()
        return_value.publicId = _public_id
        return_value.queryString = _query_string
        return_value.uploadUrl = _upload_url
        return_value.signature = _signature

        return return_value

