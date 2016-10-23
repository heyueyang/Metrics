package metrics;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 提取数据的超类。
 * <p>
 * 根据commit的排序，提取制定范围内所有commit的若干信息。若干信息的提取分别
 * 由三个子类去实现。需要注意的是，由于miningit分配给各commit的id并不是其实际提交的
 * 顺序（由于多线程并发导致），所以对于commit的排序不应根据其id排序，而应根据 commit_date排序。
 * 
 * @param sql
 *            需要连接的数据库
 * @param start
 *            指定的commit的起始值（按照时间排序）
 * @param end
 *            指定的commit的结束值（按照时间排序）
 * @see ResultSet
 * @see SQLException
 * @see Statement
 * @author niu
 *
 */
public class Extraction {
	String sql;
	SQLConnect sqlL;
	Statement stmt;
	ResultSet resultSet;
	List<Integer> commit_ids;
	private final int start;
	private final int end;

	/**
	 * 连接数据库，初始化变量值，为数据的提取做准备。
	 * 根据s和e的值，按时间顺序提取对应范围内的commit_id，若s==-1或者e==-1，则表明提取scmlog表中所以的commit_id.
	 * 否则按照时间序提取给定范围内的commit_id列表。
	 * 
	 * @param database
	 *            存放由miningit得到的数据的数据库。
	 * @param s
	 *            指定的commit的起始值，按时间排序
	 * @param e
	 *            指定的commit的结束值，按时间排序
	 * @throws Exception
	 */
	public Extraction(String database, int s, int e) throws Exception { // 包前不包后,主要为extraction1提供构造
		sqlL = new SQLConnect(database);
		this.stmt = sqlL.getStmt();
		start = s;
		end = e;
		commit_ids = new ArrayList<>();
		if (s == -1 || e == -1) {
			setCommit_idA();
		} else {
			if (s < 0) {
				throw new Exception("错误的起始序号");
			}
			setCommit_id();
		}
	}

	/**
	 * extraction2提取信息并不需要miningit生成的数据，此构造函数只是为了统一接口。
	 * 
	 * @param database
	 * @throws SQLException 
	 */
	public Extraction(String database) throws SQLException { // 为extraction2提供构造函数。
		sqlL = new SQLConnect(database);
		this.stmt = sqlL.getStmt();
		commit_ids = new ArrayList<>();
		setCommit_idA();
		start = 0; // 实际没有用到
		end = 0; // 实际没有用到
	}

	/**
	 * 按时间序返回commit_id列表。
	 * 
	 * @return 按时间排序的指定范围内的commit_id列表。
	 */
	public List<Integer> getCommit_id() {
		return commit_ids;
	}

	/**
	 * 提取scmlog中全部的commit_id（按时间排序）。
	 * 
	 * @throws SQLException
	 */
	public void setCommit_idA() throws SQLException {
		sql = "select id from scmlog order by commit_date";
		resultSet = stmt.executeQuery(sql);
		while (resultSet.next()) {
			commit_ids.add(resultSet.getInt(1));
		}
	}

	/**
	 * 提取scmlog中指定范围内按时间排序的commit_id列表。这个函数似乎是没用的，因为对于extraction1表的生成，
	 * 需要所有的commit_id。对于extraction2，其指定的序号都是全局范围内的commit_id序号
	 * 
	 * @throws SQLException
	 */
	public void setCommit_id() throws SQLException {
		commit_ids = new ArrayList<>();
		sql = "select id from scmlog order by commit_date";
		resultSet = stmt.executeQuery(sql);
		int index = 0;
		while (index < start && resultSet.next()) {
			index++;
		}
		for (int i = start; i < end; i++) {
			commit_ids.add(resultSet.getInt(1));
			resultSet.next();
		}
	}
}
