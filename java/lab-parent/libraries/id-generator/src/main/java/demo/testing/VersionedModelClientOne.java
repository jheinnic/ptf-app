package demo.testing;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.CuratorFrameworkFactory.Builder;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.framework.state.ConnectionStateListenerManagerFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.async.AsyncCuratorFramework;
import org.apache.curator.x.async.AsyncStage;
import org.apache.curator.x.async.WatchMode;
import org.apache.curator.x.async.modeled.JacksonModelSerializer;
import org.apache.curator.x.async.modeled.ModelSpec;
import org.apache.curator.x.async.modeled.ModeledFramework;
import org.apache.curator.x.async.modeled.ZPath;
import org.apache.curator.x.async.modeled.versioned.Versioned;
import org.apache.curator.x.async.modeled.versioned.VersionedModeledFramework;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.data.Stat;

import lombok.Value;

public class VersionedModelClientOne {
	public static final String LEADER_PATH = "/testing/jchDemo/v1";
	public static final String LEADER_PATH_B = "/testing/jchDemo/v2";
	public static final String LEADER_PATH_C = "/testing/jchDemo/v3";

	public static final String ID_A = "Alice";
	public static final String ID_B = "Bob";
	public static final String ID_C = "Charlie";

	public static void main(String[] args) {
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(150, 4, 10000);
		Builder curatorBuilder = CuratorFrameworkFactory.builder().connectString("localhost:2181")
				.retryPolicy(retryPolicy).connectionStateListenerManagerFactory(
						ConnectionStateListenerManagerFactory.circuitBreaking(retryPolicy))
				.canBeReadOnly(false);
		CuratorFramework curator = curatorBuilder.build();
		ConnListener connListener = new ConnListener(curator);
		curator.getConnectionStateListenable().addListener(connListener);
		try {
			curator.start();
		} catch (Exception exp) {
			System.out.println(exp.getMessage());
		}

		System.out.println("Done");
		try {
			Thread.sleep(25000);
		} catch (Exception exp) {
			System.out.println("Sleep failure: " + exp.getMessage());
		}
	}

	public static class ConnListener implements ConnectionStateListener {
		private final CuratorFramework curator;

		ConnListener(CuratorFramework curator) {
			this.curator = curator;
		}

		static final String NODE_SEQUENCE_SUFFIX = "nodeSequence";

		public void stateChanged(CuratorFramework client, ConnectionState newState) {
			System.out.println(newState);
			if (newState == ConnectionState.CONNECTED) {
				final AsyncCuratorFramework asyncCurator = AsyncCuratorFramework.wrap(curator);

				final ModelSpec<NodeSequenceModel> mySpec = ModelSpec
						.builder(ZPath.parse("/testing/demo/variants/v0").child(NODE_SEQUENCE_SUFFIX),
								JacksonModelSerializer.build(NodeSequenceModel.class))
						.build();

				final ModeledFramework<NodeSequenceModel> modelClient = ModeledFramework.builder(asyncCurator, mySpec)
						.withUnhandledErrorListener((String message, Throwable exception) -> {
							if (message != null) {
								System.out.println("Unhandled Error: " + message);
							} else {
								System.out.println("Unhandled Error: ");
							}
							if (exception != null) {
								exception.printStackTrace();
							}
						}).watched(WatchMode.stateChangeAndSuccess).build();
				final VersionedModeledFramework<NodeSequenceModel> versionClient = modelClient.versioned();

				NodeSequenceModel state = new NodeSequenceModel((byte) 1, 1L, 1L);
				Versioned<NodeSequenceModel> versionedState = Versioned.from(state, 1);
				AsyncStage<String> firstSet = versionClient.set(versionedState);
				firstSet.exceptionally(
					(Throwable err) -> {
						err.printStackTrace();
						return err.getMessage();
					}
				).thenAccept(
					(String str) -> {
						System.out.println("On version set: " + str);
					}
				);

				if (firstSet.event() == null) {
					System.out.println("No watch handler for firstSet");
				} else {
					firstSet.event().thenAccept(
						(WatchedEvent evt) -> {
							System.out.println("On version set watch: " + evt.toString());
						}
					);
				}
				
				state = new NodeSequenceModel((byte) 1, 2L, 3307L);
				versionedState = Versioned.from(state, 2);
				AsyncStage<Stat> secondSet = versionClient.update(versionedState);
				secondSet.exceptionally(
					(Throwable err) -> {
						err.printStackTrace();
						return null;
					}
				).thenAccept(
					(Stat stat) -> {
						if (stat != null) {
							System.out.println(
								String.format(
									"** Second %s: %d, %d", 
									stat.toString(), 
									stat.getVersion(), 
									stat.getDataLength()
								)
							);
						}
					}
				);
				
				state = new NodeSequenceModel((byte) 1, 3L, 930301L);
				versionedState = Versioned.from(state, 1);
				AsyncStage<Stat> thirdSet = versionClient.update(versionedState);
				thirdSet.exceptionally(
					(Throwable err) -> {
						err.printStackTrace();
						return null;
					}
				).thenAccept(
					(Stat stat) -> {
						if (stat != null) {
							System.out.println(
								String.format(
									"** Third %s: %d, %d", 
									stat.toString(), 
									stat.getVersion(), 
									stat.getDataLength()
								)
							);
						}
					}
				);
			}
		}
	}

	@Value()
	public static class NodeSequenceModel {
		byte variant;
		long exp;
		long t;
	}
}
