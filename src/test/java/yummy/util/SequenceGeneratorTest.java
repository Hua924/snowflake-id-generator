package yummy.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;

/**
 * Created by jinqinghua on 2017/1/6.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring/**/*.xml"})
@TestPropertySource(properties = {
		"sequence.work.id.store.driver=com.mysql.jdbc.Driver",
		"sequence.work.id.store=jdbc:h2:mem:dataSource",
		"sequence.work.id.store.user=sa",
		"sequence.work.id.store.password="
})
public class SequenceGeneratorTest {
	@Autowired
	private SnowflakeSequenceGenerator sequenceGenerator;

	@Test
	public void test() {
		final int threadCount = 100;
		final int idCount = 10000;
		final Set<Long> set = new ConcurrentSkipListSet<>();
		final CountDownLatch startAwait = new CountDownLatch(threadCount);
		final CountDownLatch endAwait = new CountDownLatch(threadCount);

		Thread[] threads = new Thread[threadCount];
		for (int i = 0; i < threadCount; i++) {
			threads[i] = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						startAwait.await();
						for (int i = 0; i < idCount; i++) {
							long id = sequenceGenerator.nextId();
							set.add(id);
						}
					} catch (Exception e) {
						System.out.println(e.getMessage());
					}
					endAwait.countDown();

				}
			});
		}

		for (int k = 0; k < threadCount; k++) {
			threads[k].start();
			startAwait.countDown();
		}

		try {
			endAwait.await();
		} catch (InterruptedException e) {
		}

		System.out.println(set.size());
		Long l = set.iterator().next();
		System.out.println(l);
		System.out.println(SnowflakeSequenceGenerator.SequenceDecoder.formattedDateString(l));
		System.out.println(SnowflakeSequenceGenerator.SequenceDecoder.sequence(l));
		System.out.println(SnowflakeSequenceGenerator.SequenceDecoder.timeStamp(l));
		System.out.println(SnowflakeSequenceGenerator.SequenceDecoder.workId(l));

		assert set.size() == threadCount * idCount;
	}
}
