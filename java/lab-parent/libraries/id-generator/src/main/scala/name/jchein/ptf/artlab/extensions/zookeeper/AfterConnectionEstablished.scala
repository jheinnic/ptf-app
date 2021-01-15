package name.jchein.ptf.artlab.extensions.zookeeper

import org.slf4j.LoggerFactory
import org.slf4j.Logger
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.utils.ThreadUtils
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

object AfterConnectionEstablished {
     private val log: Logger = LoggerFactory.getLogger(getClass())

    /**
     * Spawns a new new background thread that will block until a connection is available and
     * then execute the 'runAfterConnection' logic
     *
     * @param client             The curator client
     * @param runAfterConnection The logic to run
     * @return future of the task so it can be canceled, etc. if needed
     */
    def execute(
			client: CuratorFramework, runAfterConnection: Runnable
    ): CompletableFuture[Void] = {
        //Block until connected
        val executor = ThreadUtils.newSingleThreadExecutor(
            ThreadUtils.getProcessName(
                runAfterConnection.getClass()))
        val internalCall: Runnable = new Runnable() {
            override def run() = {
              try {
                client.blockUntilConnected()
                runAfterConnection.run()
              } catch {
                case e: Exception =>
                  ThreadUtils.checkInterrupted(e)
                  log.error("An error occurred blocking until a connection is available", e)
              } finally {
                executor.shutdown()
              }
            }
        }
        CompletableFuture.runAsync(internalCall, executor)
    }
     
     def supply[U](
			client: CuratorFramework, supplyAfterConnection: Supplier[U]
    ): CompletableFuture[U] = {
        //Block until connected
        val executor = ThreadUtils.newSingleThreadExecutor(
            ThreadUtils.getProcessName(
                supplyAfterConnection.getClass()))
        val internalCall: Supplier[U] = new Supplier[U]() {
            override def get() = {
              try {
                client.blockUntilConnected()
                supplyAfterConnection.get()
              } catch {
                case e: Exception =>
                  ThreadUtils.checkInterrupted(e)
                  log.error("An error occurred blocking until a connection is available", e)
                  throw e
              } finally {
                executor.shutdown()
              }
            }
        }
        CompletableFuture.supplyAsync(internalCall, executor)
    }
}