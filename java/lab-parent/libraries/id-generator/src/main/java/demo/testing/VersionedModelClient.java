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

public class VersionedModelClient {
	static final String LEADER_PATH_A = "/testing/jchDemo/v1";
	static final String LEADER_PATH_B = "/testing/jchDemo/v2";
	static final String LEADER_PATH_C = "/testing/jchDemo/v3";
	static final String NODE_SEQUENCE_SUFFIX = "nodeSequence";

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

		public void stateChanged(CuratorFramework client, ConnectionState newState) {
			System.out.println(newState);
			if (newState == ConnectionState.CONNECTED) {
				final AsyncCuratorFramework asyncCurator = AsyncCuratorFramework.wrap(curator);
				final JacksonModelSerializer<NodeSequenceModel> modelSeriaizer = JacksonModelSerializer.build(NodeSequenceModel.class);
				
				NodeSequenceModel state = new NodeSequenceModel((byte) 1, 1L, 1L);
				VersionedModeledFramework<NodeSequenceModel> versionClient = getVersionedClient(asyncCurator, modelSeriaizer, LEADER_PATH_A);
				Stat firstCallStat = appendNodeSequence(versionClient, state, "First");

				state = new NodeSequenceModel((byte) 1, 2L, 3307L);
				versionClient = getVersionedClient(asyncCurator, modelSeriaizer, LEADER_PATH_B);
				Stat secondCallStat = appendNodeSequence(versionClient, state, "Second");
			
				state = new NodeSequenceModel((byte) 1, 3L, 930301L);
				versionClient = getVersionedClient(asyncCurator, modelSeriaizer, LEADER_PATH_C);
				Stat thirdCallStat = appendNodeSequence(versionClient, state, "Third");
				
				System.out.println(
					String.format(
						"Stats: [{}, {}. {}]",
						firstCallStat, secondCallStat, thirdCallStat)
				);
			}
		}

		private VersionedModeledFramework<NodeSequenceModel> getVersionedClient(
				final AsyncCuratorFramework asyncCurator,
				final JacksonModelSerializer<NodeSequenceModel> modelSeriaizer, final String stateRoot) {
			final ModelSpec<NodeSequenceModel> mySpec = ModelSpec
				.builder(
					getNodeSeqZPath(stateRoot), modelSeriaizer)
				.build();
			final VersionedModeledFramework<NodeSequenceModel> versionClient = ModeledFramework.builder(asyncCurator, mySpec)
				.withUnhandledErrorListener(
					(String message, Throwable exception) -> {
						if (message != null) {
							System.out.println("Unhandled Error: " + message);
						} else {
							System.out.println("Unhandled Error: ");
						}
						if (exception != null) {
							exception.printStackTrace();
						}
					})
				.watched(WatchMode.stateChangeAndSuccess)
				.build()
				.versioned();
			return versionClient;
		}

		private ZPath getNodeSeqZPath(String stateRoot) {
			return ZPath.parse(stateRoot).child(NODE_SEQUENCE_SUFFIX);
		}

		private Stat appendNodeSequence(final VersionedModeledFramework<NodeSequenceModel> versionClient,
				NodeSequenceModel state, String upperPosition) {
			final Stat stat = new Stat();
			final String position = upperPosition.toLowerCase();
			final Versioned<NodeSequenceModel> versionedState = Versioned.from(state, 2);
			final AsyncStage<String> setCall = versionClient.set(versionedState, stat);
			setCall.exceptionally(
				(Throwable err) -> {
					err.printStackTrace();
					return err.getMessage();
				}
			).thenAccept(
				(String str) -> {
					System.out.println(
						String.format("On {} set: {}", position, str)
					);
					if (stat != null) {
						System.out.println(
							String.format(
								"** %s %s: %d, %d", 
								upperPosition,
								stat.toString(), 
								stat.getVersion(), 
								stat.getDataLength()
							)
						);
					}
				}
			);
			if (setCall.event() == null) {
				System.out.println(
					String.format("No watch handler for {} set", position)
				);
			} else {
				setCall.event().thenAccept(
					(WatchedEvent evt) -> {
						System.out.println(
							String.format("On {} set watch: {}", position, evt)
						);
					}
				);
			}
			return stat;
		}
	}

	@Value()
	public static class NodeSequenceModel {
		byte variant;
		long exp;
		long t;
	}
}
