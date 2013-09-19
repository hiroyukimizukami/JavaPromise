package jp.plusc.javapromise;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PromiseTest {
	private static final String endPointHuga = "http://hugahuga/huga";
	private static final String endPointHoge = "http://hogehoge/hoge";
	private static final String endPointMoga = "http://mogamoga/moga";
	private static final String endPointMuga = "http://mugamuga/muga";
	private static final Logger logger = LoggerFactory.getLogger(PromiseTest.class);

	@Test
	public void testNewOk() {
		new Promise<String>();
		new Promise<String>(new Function<String>() {
			@Override
			protected void impl(String val) {
				;
			}
		});
		Assert.assertTrue(true);
	}

	@Test
	public void testInnerClassFunction() {
		final Function<String> f = new Function<String>() {
			@Override
			protected void impl(final String val) {
				Assert.assertNotNull(next());
			}
		};
		f.call(null);
		Assert.assertTrue(true);
	}

	public void testRunEmptyPromise() {
		new Promise<String>().run();
		Assert.assertTrue(true);
	}

	@Test(timeout=1000)
	public void testPromiseWithFunction() {

		final DummyAsyncTask async = new DummyAsyncTask(2);
		final Promise<String> p = new Promise<String>();

		final Function<String> fa = new Function<String>() {

			@Override
			protected void impl(final String val) {
				Assert.assertNull(val);

				next().call("1");
			}
		};

		final Function<String> fb = new Function<String>() {

			@Override
			protected void impl(final String val) {
				Assert.assertEquals("1", String.valueOf(val));

				final Map<String, Object> params = new HashMap<String, Object>();
				params.put("args", "hoge");

				async.connectAsync(endPointHoge, params, new DummyAsyncTask.Callbackable() {

					@Override
					protected void impl(final Map<String, String>  result) {
						Assert.assertEquals(endPointHoge, result.get(DummyAsyncTask.URL));
						Assert.assertEquals(String.valueOf(params), result.get(DummyAsyncTask.PARAMS));
						next().call(String.valueOf(val) + String.valueOf("2"));
					}
				});
			}
		};

		final Function<String> fc = new Function<String>() {

			@Override
			protected void impl(final String val) {
				Assert.assertEquals("12", String.valueOf(val));

				final Map<String, Object> params = new HashMap<String, Object>();
				params.put("args", "huga");

				async.connectAsync(endPointHuga, params, new DummyAsyncTask.Callbackable() {

					@Override
					protected void impl(final Map<String, String>  result) {
						Assert.assertEquals(endPointHuga, result.get(DummyAsyncTask.URL));
						Assert.assertEquals(String.valueOf(params), result.get(DummyAsyncTask.PARAMS));
					}
				});
			}
		};

		p.bind(fa).bind(fb, fc).run();
		async.waitTask();
		Assert.assertTrue(async.getThrowables().isEmpty());
	}

	@Test(timeout=1000)
	public void testPromiseWithPromise() {

		final DummyAsyncTask async = new DummyAsyncTask(4);
		final Promise<String> p = new Promise<String>();

		final Promise<String> pa = getEmptyPromiseWith(new Function<String>() {

			@Override
			protected void impl(final String val) {
				Assert.assertNull(val);
				next().call("1");
			}
		});
		final Promise<String> pb = getEmptyPromiseWith(new Function<String>() {

			@Override
			protected void impl(final String  val) {
				Assert.assertEquals("1", String.valueOf(val));

				final Map<String, Object> params = new HashMap<String, Object>();
				params.put("args", "hoge");

				async.connectAsync(endPointHoge, params, new DummyAsyncTask.Callbackable() {
					@Override
					protected void impl(final Map<String, String>  result) {
						Assert.assertEquals(endPointHoge, result.get(DummyAsyncTask.URL));
						Assert.assertEquals(String.valueOf(params), result.get(DummyAsyncTask.PARAMS));

						next().call(String.valueOf(val) + String.valueOf("2"));
					}
				});
			}
		});

		final Function<String> fa = new Function<String>() {

			@Override
			protected void impl(final String  val) {
				Assert.assertEquals("12", String.valueOf(val));

				final Map<String, Object> params = new HashMap<String, Object>();
				params.put("args", "huga");
				async.connectAsync(endPointHuga, params, new DummyAsyncTask.Callbackable() {

					@Override
					protected void impl(final Map<String, String>  result) {

						Assert.assertEquals(endPointHuga, result.get(DummyAsyncTask.URL));
						Assert.assertEquals(String.valueOf(params), result.get(DummyAsyncTask.PARAMS));

						next().call(String.valueOf(val) + String.valueOf("3"));
					}
				});
			}
		};

		final Function<String> fb = new Function<String>() {

			@Override
			protected void impl(final String  val) {
				Assert.assertEquals("123", String.valueOf(val));

				final Map<String, Object> params = new HashMap<String, Object>();
				params.put("args", "moga");

				async.connectAsync(endPointMoga, params, new DummyAsyncTask.Callbackable() {

					@Override
					protected void impl(final Map<String, String>  result) {
						Assert.assertEquals(endPointMoga, result.get(DummyAsyncTask.URL));
						Assert.assertEquals(String.valueOf(params), result.get(DummyAsyncTask.PARAMS));

						next().call(String.valueOf(val) + String.valueOf("4"));
					}
				});
			}
		};

		pb.bind(fa, fb);

		final Promise<String> pc = getEmptyPromiseWith(new Function<String>() {

			@Override
			protected void impl(final String  val) {
				Assert.assertEquals("1234", String.valueOf(val));

				final Map<String, Object> params = new HashMap<String, Object>();
				params.put("args", "muga");

				async.connectAsync(endPointMuga, params, new DummyAsyncTask.Callbackable() {

					@Override
					protected void impl(final Map<String, String>  result) {
						Assert.assertEquals(endPointMuga, result.get(DummyAsyncTask.URL));
						Assert.assertEquals(String.valueOf(params), result.get(DummyAsyncTask.PARAMS));
					}
				});
			}
		});

		p.bind(pa).bind(pb, pc).run();
		async.waitTask();
		Assert.assertTrue(async.getThrowables().isEmpty());
	}

	@Test
	public void testRunWithArguments() {
		Promise<String> p = new Promise<String>();
		final Promise<String> pa = getEmptyPromiseWith(new Function<String>() {

			@Override
			protected void impl(final String  val) {
				Assert.assertNotNull(val);
				Assert.assertEquals("hoge", String.valueOf(val));

				next().call(null);
			}
		});

		p.bind(pa).run("hoge");
	}

	public void testBreakChainIntentionally() {
		final Map<String, Integer> holder = new HashMap<String, Integer>();
		Promise<String> p = new Promise<String>();
		final Promise<String> pa = getEmptyPromiseWith(new Function<String>() {
			@Override
			protected void impl(final String  val) {
				holder.put("increment", 0);

				next().call(null);
			}
		});

		final Promise<String> pb = getEmptyPromiseWith(new Function<String>() {

			@Override
			protected void impl(final String  val) {
				holder.put("increment", 1);
				//getNext is not called here.
			}
		});

		final Promise<String> pc = getEmptyPromiseWith(new Function<String>() {

			@Override
			protected void impl(final String  val) {
				// This Promise<String> must not be called.
				Assert.assertTrue(false);
			}
		});

		p.bind(pa, pb, pc).run();
		Assert.assertTrue(holder.get("increment") == 1);
	}


	private static Promise<String> getEmptyPromiseWith(Function<String> f) {
		return new Promise<String>(f);
	}
}