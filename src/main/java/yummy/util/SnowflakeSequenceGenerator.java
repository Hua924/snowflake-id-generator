package yummy.util;


import org.springframework.beans.factory.annotation.Autowired;
import yummy.util.support.WorkIdStore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

public class SnowflakeSequenceGenerator {
	private static final long GENESIS = 1429113600000l;// 2015-04-16 00:00:00 GMT+0800

	private static final long WORK_ID_BITS = 10l;

	private static final long SEQUENCE_BITS = 12l;

	private static final long WORK_ID_LEFT_SHIFT = SEQUENCE_BITS;

	private static final long TIME_STAMP_LEFT_SHIFT = WORK_ID_BITS + SEQUENCE_BITS;

	private static final long SEQUENCE_MASK = -1 ^ (-1 << SEQUENCE_BITS);


	private long sequence = 0l;

	private long lastTimestamp = -1l;

	private final ReentrantLock lock = new ReentrantLock();

	private final WorkIdStore workIdStore;

	private final long workId;

	public SnowflakeSequenceGenerator(@Autowired WorkIdStore workIdStore) {
		this.workIdStore = workIdStore;
		this.workId = workIdStore.workId();
//		if (workId == -1L) {
//			throw new RuntimeException("SnowflakeSequenceGenerator initialization failure,node work id error");
//		}
	}

	public long nextId() {
		long nexdId = -1l;

		lock.lock();
		try {
			long timeStamp = timeGen();
			if (lastTimestamp == timeStamp) {
				sequence = (sequence + 1) & SEQUENCE_MASK;
				if (sequence == 0l) {
					timeStamp = tillNextMills(lastTimestamp);
				}
			} else {
				sequence = 0l;
			}

			if (timeStamp < lastTimestamp) {
				throw new RuntimeException("system clock fall back");
			}

			lastTimestamp = timeStamp;

			nexdId = ((timeStamp - GENESIS) << TIME_STAMP_LEFT_SHIFT) | (workId << WORK_ID_LEFT_SHIFT) | (sequence);

		} finally {
			lock.unlock();
		}
		return nexdId;
	}

	private static long timeGen() {
		return System.currentTimeMillis();
	}

	private static long tillNextMills(long lastTimestamp) {
		long timeStamp = timeGen();
		while (timeStamp <= lastTimestamp) {
			timeStamp = timeGen();
		}
		return timeStamp;
	}


	public static class SequenceDecoder {
		private static long timeStampMask = -1l ^ (-1l << 41);

		private static long workIdMask = -1l ^ (-1l << WORK_ID_BITS);

		private static long sequenceMask = -1l ^ (-1l << SEQUENCE_BITS);

		private static long workIdRightShift = SEQUENCE_BITS;

		private static long timeStampRightShift = WORK_ID_BITS + SEQUENCE_BITS;

		public static long sequence(long l) {
			return l & sequenceMask;
		}

		public static long workId(long l) {
			return (l >> workIdRightShift) & workIdMask;
		}

		public static long timeStamp(long l) {
			return (l >> timeStampRightShift) & timeStampMask;
		}

		public static String formattedDateString(long l) {
			long timeStamp = timeStamp(l);
			long real = timeStamp + GENESIS;
			SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmssS");
			return fmt.format(new Date(real));
		}
	}


}
