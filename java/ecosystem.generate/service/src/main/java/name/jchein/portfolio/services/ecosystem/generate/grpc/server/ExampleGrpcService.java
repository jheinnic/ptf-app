package name.jchein.portfolio.services.ecosystem.generate.grpc.server;


import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;

import io.eventuate.EntityWithIdAndVersion;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.springboot.autoconfigure.grpc.server.GrpcService;

import name.jchein.portfolio.common.grpc.action.BasicReply;
import name.jchein.portfolio.common.grpc.action.StatusCode;
import name.jchein.portfolio.common.grpc.interceptor.EnrichHeaderServerInterceptor;

import name.jchein.portfolio.services.ecosystem.generate.backend.IExampleDomainService;
import name.jchein.portfolio.services.ecosystem.generate.domain.Example;
import name.jchein.portfolio.services.ecosystem.generate.grpc.proto.CreateExampleRequest;
import name.jchein.portfolio.services.ecosystem.generate.grpc.proto.ExampleGrpc;


@Slf4j
@GrpcService(value = ExampleGrpc.class, interceptors = {
   EnrichHeaderServerInterceptor.class
})
public class ExampleGrpcService
extends ExampleGrpc.ExampleImplBase
{
   private final IExampleDomainService domainService;


   ExampleGrpcService( @Autowired IExampleDomainService domainService )
   {
      this.domainService = domainService;
   }


   /**
   */
   @Override
   public void createExample(CreateExampleRequest request, StreamObserver<BasicReply> responseObserver)
   {
      String uuid = request.getId();
      Preconditions.checkNotNull(uuid);
      log.info("Processing call to createExample()");

      try {
         EntityWithIdAndVersion<Example> idVersion =
            this.domainService.createExample(
               request.getId(),
               request.getPilotId(),
               request.getFirstName(),
               request.getMiddleName(),
               request.getLastName());
         if ((idVersion == null) || (!uuid.equals(idVersion.getEntityId()))) {
            log.warn("{} was null or has wrong aggregate ID", idVersion);
            responseObserver.onNext(
               BasicReply.newBuilder()
                  .setMessage("OK")
                  .setStatus(StatusCode.INVALID_REQUEST)
                  .build());
         } else {
            responseObserver.onNext(
               BasicReply.newBuilder()
                  .setMessage("OK")
                  .setStatus(StatusCode.OK)
                  .build());
         }
         responseObserver.onCompleted();
      }
      catch (Throwable err) {
         log.error("Exception thrown during createExample call: ", err);
         responseObserver.onNext(
            BasicReply.newBuilder()
               .setMessage(err.getMessage())
               .setStatus(StatusCode.INTERNAL_ERROR)
               .build());
         responseObserver.onError(err);
      }
   }
}
