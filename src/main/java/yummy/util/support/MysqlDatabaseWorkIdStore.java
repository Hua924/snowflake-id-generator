package yummy.util.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.FileSystems;
import java.sql.*;


public class MysqlDatabaseWorkIdStore implements EnvironmentAware, WorkIdStore {
	Log log = LogFactory.getLog(getClass());

	private static final String STORE_DRIVER_CLASS = "sequence.work.id.store.driver";
	private static final String STORE_URL_KEY = "sequence.work.id.store";
	private static final String STORE_USER_KEY = "sequence.work.id.store.user";
	private static final String STORE_PWD_KEY = "sequence.work.id.store.password";
	private static final String SERVICE_WORK_PATH = FileSystems.getDefault().getPath("").toFile().getAbsolutePath();

	private Environment environment;

	private String storeDriverClass;

	private String storeURL;

	private String storeUser;

	private String storePwd;

	private String hostIP;

	private long workId = -1L;

	private final Object monitor = new Object();

	@Override
	public long workId() {
		return this.workId;
	}

	private long doInit() throws ClassNotFoundException, SocketException, UnknownHostException {
		Class.forName(storeDriverClass);
		hostIP = LocalHostUtil.hostAddress();
		long workId = -1l;

		workId = loadWorkIdFromStore();

		if (workId == -1l) {
			workId = requestWorkIdFromStore();
		}

		if (workId == -1l) {
			throw new RuntimeException(
					"sequence generator cannot be allotted a correct work id，cannot startup application");
		}

		return workId;
	}

	private int requestWorkIdFromStore() {
		int workId = -1;
		StringBuilder stringBuilder = new StringBuilder("insert into work_id_store (ip,service_work_path) values (?,?)");
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			connection = DriverManager.getConnection(storeURL, storeUser, storePwd);
			preparedStatement = connection.prepareStatement(stringBuilder.toString(),
					PreparedStatement.RETURN_GENERATED_KEYS);
			preparedStatement.setString(1, hostIP);
			preparedStatement.setString(2, SERVICE_WORK_PATH);
			preparedStatement.executeUpdate();
			resultSet = preparedStatement.getGeneratedKeys();
			if (resultSet.next()) {
				workId = resultSet.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				releaseDBResources(connection, preparedStatement, resultSet);
			} catch (SQLException e) {
			}
		}

		return workId;
	}

	private int loadWorkIdFromStore() {
		int workId = -1;
		StringBuilder stringBuilder = new StringBuilder("select id from work_id_store t where ");
		stringBuilder.append("t.ip = '").append(hostIP).append("' ");
		stringBuilder.append("and ");
		stringBuilder.append("t.service_work_path = '").append(SERVICE_WORK_PATH).append("'");

		Connection connection = null;
		Statement stmt = null;
		ResultSet resultSet = null;
		try {
			connection = DriverManager.getConnection(storeURL, storeUser, storePwd);
			stmt = connection.createStatement();
			resultSet = stmt.executeQuery(stringBuilder.toString());
			if (resultSet.next()) {
				workId = resultSet.getInt("id");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				releaseDBResources(connection, stmt, resultSet);
			} catch (SQLException e) {
			}
		}

		return workId;
	}

	private void releaseDBResources(Connection connection, Statement stmt, ResultSet resultSet) throws SQLException {
		if (connection != null)
			connection.close();
		if (stmt != null)
			stmt.close();
		if (resultSet != null)
			resultSet.close();
	}

	@PostConstruct
	public void initial() throws Exception {
		storeDriverClass = this.environment.getProperty(STORE_DRIVER_CLASS);
		storeURL = this.environment.getProperty(STORE_URL_KEY);
		storeUser = this.environment.getProperty(STORE_USER_KEY);
		storePwd = this.environment.getProperty(STORE_PWD_KEY);
		if (log.isDebugEnabled()) {
			log.info("id generator work store driver class:" + storeDriverClass);
			log.info("id generator work store url:" + storeURL);
			log.info("id generator work store user:" + storeUser);
			log.info("id generator work store passwd:" + storePwd);
		}
		if (storeDriverClass == null || storeDriverClass.trim().equals("")) {
			throw new RuntimeException("work id store driver class not config");
		}

		if (storeURL == null || storeURL.trim().equals("")) {
			throw new RuntimeException("work id store url not config");
		}

		if (storeUser == null) {
			throw new RuntimeException("work id store user not config");
		}

		if (storePwd == null) {
			throw new RuntimeException("work id store pwd not config");
		}

		synchronized (monitor) {
			if (workId == -1L) {
				workId = doInit();
			}
		}
	}

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}
}
