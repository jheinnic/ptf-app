# Generated by the gRPC Python protocol compiler plugin. DO NOT EDIT!
"""Client and server classes corresponding to protobuf-defined services."""
import grpc

from v1 import signed_image_uploads_pb2 as v1_dot_signed__image__uploads__pb2


class ImageUploadSigningServiceStub(object):
    """Service for retrieving signed image upload URLs
    """

    def __init__(self, channel):
        """Constructor.

        Args:
            channel: A grpc.Channel.
        """
        self.createSignedUpload = channel.unary_unary(
                '/signed_image_uploads.ImageUploadSigningService/createSignedUpload',
                request_serializer=v1_dot_signed__image__uploads__pb2.CreateSignedUploadSingleRequest.SerializeToString,
                response_deserializer=v1_dot_signed__image__uploads__pb2.SignedUploadSingleReply.FromString,
                )
        self.createSignedUploadBatch = channel.unary_unary(
                '/signed_image_uploads.ImageUploadSigningService/createSignedUploadBatch',
                request_serializer=v1_dot_signed__image__uploads__pb2.CreateSignedUploadBatchRequest.SerializeToString,
                response_deserializer=v1_dot_signed__image__uploads__pb2.SignedUploadBatchReply.FromString,
                )
        self.createSignedUploadCursor = channel.unary_unary(
                '/signed_image_uploads.ImageUploadSigningService/createSignedUploadCursor',
                request_serializer=v1_dot_signed__image__uploads__pb2.CreateSignedUploadCursorRequest.SerializeToString,
                response_deserializer=v1_dot_signed__image__uploads__pb2.SignedUploadCursorReply.FromString,
                )
        self.continueSignedUploadCursor = channel.unary_unary(
                '/signed_image_uploads.ImageUploadSigningService/continueSignedUploadCursor',
                request_serializer=v1_dot_signed__image__uploads__pb2.ContinueSignedUploadCursorRequest.SerializeToString,
                response_deserializer=v1_dot_signed__image__uploads__pb2.SignedUploadCursorReply.FromString,
                )


class ImageUploadSigningServiceServicer(object):
    """Service for retrieving signed image upload URLs
    """

    def createSignedUpload(self, request, context):
        """Missing associated documentation comment in .proto file."""
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def createSignedUploadBatch(self, request, context):
        """Missing associated documentation comment in .proto file."""
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def createSignedUploadCursor(self, request, context):
        """Missing associated documentation comment in .proto file."""
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def continueSignedUploadCursor(self, request, context):
        """Missing associated documentation comment in .proto file."""
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')


def add_ImageUploadSigningServiceServicer_to_server(servicer, server):
    rpc_method_handlers = {
            'createSignedUpload': grpc.unary_unary_rpc_method_handler(
                    servicer.createSignedUpload,
                    request_deserializer=v1_dot_signed__image__uploads__pb2.CreateSignedUploadSingleRequest.FromString,
                    response_serializer=v1_dot_signed__image__uploads__pb2.SignedUploadSingleReply.SerializeToString,
            ),
            'createSignedUploadBatch': grpc.unary_unary_rpc_method_handler(
                    servicer.createSignedUploadBatch,
                    request_deserializer=v1_dot_signed__image__uploads__pb2.CreateSignedUploadBatchRequest.FromString,
                    response_serializer=v1_dot_signed__image__uploads__pb2.SignedUploadBatchReply.SerializeToString,
            ),
            'createSignedUploadCursor': grpc.unary_unary_rpc_method_handler(
                    servicer.createSignedUploadCursor,
                    request_deserializer=v1_dot_signed__image__uploads__pb2.CreateSignedUploadCursorRequest.FromString,
                    response_serializer=v1_dot_signed__image__uploads__pb2.SignedUploadCursorReply.SerializeToString,
            ),
            'continueSignedUploadCursor': grpc.unary_unary_rpc_method_handler(
                    servicer.continueSignedUploadCursor,
                    request_deserializer=v1_dot_signed__image__uploads__pb2.ContinueSignedUploadCursorRequest.FromString,
                    response_serializer=v1_dot_signed__image__uploads__pb2.SignedUploadCursorReply.SerializeToString,
            ),
    }
    generic_handler = grpc.method_handlers_generic_handler(
            'signed_image_uploads.ImageUploadSigningService', rpc_method_handlers)
    server.add_generic_rpc_handlers((generic_handler,))


 # This class is part of an EXPERIMENTAL API.
class ImageUploadSigningService(object):
    """Service for retrieving signed image upload URLs
    """

    @staticmethod
    def createSignedUpload(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            insecure=False,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_unary(request, target, '/signed_image_uploads.ImageUploadSigningService/createSignedUpload',
            v1_dot_signed__image__uploads__pb2.CreateSignedUploadSingleRequest.SerializeToString,
            v1_dot_signed__image__uploads__pb2.SignedUploadSingleReply.FromString,
            options, channel_credentials,
            insecure, call_credentials, compression, wait_for_ready, timeout, metadata)

    @staticmethod
    def createSignedUploadBatch(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            insecure=False,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_unary(request, target, '/signed_image_uploads.ImageUploadSigningService/createSignedUploadBatch',
            v1_dot_signed__image__uploads__pb2.CreateSignedUploadBatchRequest.SerializeToString,
            v1_dot_signed__image__uploads__pb2.SignedUploadBatchReply.FromString,
            options, channel_credentials,
            insecure, call_credentials, compression, wait_for_ready, timeout, metadata)

    @staticmethod
    def createSignedUploadCursor(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            insecure=False,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_unary(request, target, '/signed_image_uploads.ImageUploadSigningService/createSignedUploadCursor',
            v1_dot_signed__image__uploads__pb2.CreateSignedUploadCursorRequest.SerializeToString,
            v1_dot_signed__image__uploads__pb2.SignedUploadCursorReply.FromString,
            options, channel_credentials,
            insecure, call_credentials, compression, wait_for_ready, timeout, metadata)

    @staticmethod
    def continueSignedUploadCursor(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            insecure=False,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_unary(request, target, '/signed_image_uploads.ImageUploadSigningService/continueSignedUploadCursor',
            v1_dot_signed__image__uploads__pb2.ContinueSignedUploadCursorRequest.SerializeToString,
            v1_dot_signed__image__uploads__pb2.SignedUploadCursorReply.FromString,
            options, channel_credentials,
            insecure, call_credentials, compression, wait_for_ready, timeout, metadata)
