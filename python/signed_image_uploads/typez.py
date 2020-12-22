import abc
from typing import NamedTuple

from signed_image_uploads_pb2 import CreateSingleSignedUpload, BeginSignedUploadSeries, ContinueSignedUploadSeries, \
    SignedUploadRequestResult


class IUploadHelper(abc.ABC):
    @abc.abstractmethod
    def sign_upload_request(self, request: CreateSingleSignedUpload) -> SignedUploadRequestResult:
        pass

    @abc.abstractmethod
    def sign_first_upload_request(self, request: BeginSignedUploadSeries) -> SignedUploadRequestResult:
        pass

    @abc.abstractmethod
    def sign_next_upload_request(self, request: ContinueSignedUploadSeries) -> SignedUploadRequestResult:
        pass


class IPublicIdHelper(abc.ABC):
    @abc.abstractmethod
    def get_new_id(self, is_series: bool = False) -> str:
        pass

    @abc.abstractmethod
    def get_next_id(self, last_allocation: str) -> str:
        pass
