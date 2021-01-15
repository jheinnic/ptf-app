package demo.testing;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.CuratorFrameworkFactory.Builder;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatch.CloseMode;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.framework.state.ConnectionStateListenerManagerFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.async.AsyncCuratorFramework;

public class CuratorClient {
	public static final String LEADER_PATH = "/testing/jchDemo/v1";
	public static final String LEADER_PATH_B = "/testing/jchDemo/v2";
	public static final String LEADER_PATH_C = "/testing/jchDemo/v3";
	
	public static final String ID_A = "Alice";
	public static final String ID_B = "Bob";
	public static final String ID_C = "Charlie";

	public static void main(String[] args) {
		RetryPolicy retryPolicy =
			new ExponentialBackoffRetry(150, 4, 10000);
		Builder curatorBuilder = CuratorFrameworkFactory.builder()
			.connectString("localhost:2181")
			.retryPolicy(retryPolicy)
			.connectionStateListenerManagerFactory(
				ConnectionStateListenerManagerFactory.circuitBreaking(retryPolicy)
			).canBeReadOnly(false);
		CuratorFramework curator = curatorBuilder.build();
		AsyncCuratorFramework asyncCurator = AsyncCuratorFramework.wrap(curator);
		ConnListener connListener = new ConnListener();
		curator.getConnectionStateListenable()
			.addListener(connListener);
		LeaderLatch leaderLatch = new LeaderLatch(curator, LEADER_PATH, "jchDemo", CloseMode.NOTIFY_LEADER);
		LeaderListener leaderListener = new LeaderListener(leaderLatch);
		leaderLatch.addListener(leaderListener);
		try {
			curator.start();
			leaderLatch.start();
		} catch (Exception exp) {
			System.out.println(exp.getMessage());
		}

		System.out.println("Done");
//		try {
//			Thread.sleep(7500);
//		} catch(Exception exp) {
//			System.out.println(
//				"Sleep failure: " + exp.getMessage());
//		}
//
//		try {
//			leaderLatch.close();
//		} catch(Exception exp) {
//			System.out.println(exp.getMessage());
//		}
		
		try {
			Thread.sleep(8000);
		} catch(Exception exp) {
			System.out.println(
				"Sleep failure: " + exp.getMessage());
		}
		System.out.println("Start executors");
		
		Executor pool = Executors.newFixedThreadPool(8);
		RunForOffice[] tasksA = new RunForOffice[] {
			new RunForOffice(LEADER_PATH_B, ID_B, curator),
			new RunForOffice(LEADER_PATH_C, ID_C, curator),
			new RunForOffice(LEADER_PATH_B, ID_A, curator),
			new RunForOffice(LEADER_PATH_C, ID_B, curator),
		};
		RunForLater[] tasksB = new RunForLater[] {
			new RunForLater(LEADER_PATH, ID_B, curator),
			new RunForLater(LEADER_PATH, ID_C, curator),
			new RunForLater(LEADER_PATH_C, ID_A, curator),
			new RunForLater(LEADER_PATH_B, ID_C, curator)
		};
		pool.execute(tasksA[0]);
		pool.execute(tasksB[0]);
		pool.execute(tasksA[1]);
		pool.execute(tasksB[1]);
		pool.execute(tasksA[2]);
		pool.execute(tasksB[2]);
		pool.execute(tasksA[3]);
		pool.execute(tasksB[3]);
	
		System.out.println("Done2");
		try {
			Thread.sleep(20000);
		} catch(Exception exp) {
			System.out.println(
				"Sleep failure: " + exp.getMessage());
		}
		tasksA[0].check();
		tasksB[0].check();
		tasksA[1].check();
		tasksB[1].check();
		tasksA[2].check();
		tasksB[2].check();
		tasksA[3].check();
		tasksB[3].check();

		try {
			Thread.sleep(10000);
		} catch(Exception exp) {
			System.out.println(
				"Sleep failure: " + exp.getMessage());
		}
		System.out.println("Done3");

	}
	
	public static class ConnListener implements ConnectionStateListener {
		public void stateChanged(CuratorFramework client, ConnectionState newState) {
			System.out.println(newState);
		}
	}
	
	public static class LeaderListener implements LeaderLatchListener {
		private final LeaderLatch leaderLatch;

		public LeaderListener(final LeaderLatch leaderLatch) {
			this.leaderLatch = leaderLatch;
		}

		public void isLeader() {
			try {
				System.out.println(
					"Leader for " + this.leaderLatch.getId() + "@" + this.leaderLatch.getOurPath() + ": " + this.leaderLatch.getLeader() + ", " + this.leaderLatch.hasLeadership());
			} catch(Exception exp) {
				System.out.println(exp.getMessage());
			}
		}

		public void notLeader() {
			try {
				System.out.println(
					"Not leader for " + this.leaderLatch.getId() + "@" + this.leaderLatch.getOurPath() + ": " + this.leaderLatch.getLeader() + ", " + this.leaderLatch.hasLeadership());
			} catch(Exception exp) {
				System.out.println(exp.getMessage());
			}
		}
	}
	
	public static class RunForLater implements Runnable {
		private final String path;
		private final String id;
		private final CuratorFramework curator;
		private LeaderLatch latch = null;
		
		public RunForLater(
			String path, String id, CuratorFramework curator
		) {
			this.path = path;
			this.id = id;
			this.curator = curator;
		}

		public void run() {
			this.latch =
				new LeaderLatch(this.curator, this.path, this.id, CloseMode.NOTIFY_LEADER);
			final LeaderListener listener = new LeaderListener(latch);
			latch.addListener(listener);
			try {
				System.out.println("Starting " + this.id + "@" + this.path);
				latch.start();
				System.out.println("Started " + this.id + "@" + this.path);
//				Thread.sleep(5000);
//				System.out.println("Closing " + this.id + "@" + this.path);
//				latch.close();
//				System.out.println("Closed " + this.id + "@" + this.path);
			} catch(Exception exp) {
				System.out.println("!! Error: " + this.id + "@" + this.path);
				exp.printStackTrace();
			}
		}
		
		public void check() {
			try {
	  			System.out.println(
	  				String.format(
	  					"%s, %s, %s, %s, %s",
	  					this.latch.getId(),
	  					this.latch.getOurPath(),
	  					this.latch.hasLeadership(),
	  					this.latch.getState(),
	  					this.latch.getLeader()
	  				)
	  			);
	  			this.latch.close();
			} catch(Exception exp) {
				System.out.println("!! Error: " + this.id + "@" + this.path);
				exp.printStackTrace();
			}
		}
	}
	
	public static class RunForOffice implements Runnable {
		private final String path;
		private final String id;
		private final LeaderLatch latch;
		
		public RunForOffice(
			String path, String id, CuratorFramework curator
		) {
			this.path = path;
			this.id = id;
			this.latch = new LeaderLatch(curator, path, id, CloseMode.NOTIFY_LEADER);
			LeaderListener listener = new LeaderListener(this.latch);
			this.latch.addListener(listener);
		}

		public void run() {
			try {
				System.out.println("Starting " + this.id + "@" + this.path);
				this.latch.start();
				System.out.println("Started " + this.id + "@" + this.path);
//				Thread.sleep(1500);
//				System.out.println("Closing " + this.id + "@" + this.path);
//				this.latch.close();
//				System.out.println("Closed " + this.id + "@" + this.path);
			} catch(Exception exp) {
				System.out.println("!! Error: " + this.id + "@" + this.path);
				exp.printStackTrace();
			}
		}
		
		public void check() {
			try {
	  			System.out.println(
	  				String.format(
	  					"%s, %s, %s, %s, %s",
	  					this.latch.getId(),
	  					this.latch.getOurPath(),
	  					this.latch.hasLeadership(),
	  					this.latch.getState(),
	  					this.latch.getLeader()
	  				)
	  			);
	  			this.latch.close();
			} catch(Exception exp) {
				System.out.println("!! Error: " + this.id + "@" + this.path);
				exp.printStackTrace();
			}
		}
	}
}
