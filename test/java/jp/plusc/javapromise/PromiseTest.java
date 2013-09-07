package jp.plusc.javapromise;

import static jp.plusc.javapromise.Throws.livesOk;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import jp.plusc.javapromise.Promise.Function;
import jp.plusc.javapromise.Throws.Block;

import org.junit.Assert;
import org.junit.Test;

public class PromiseTest {

	@Test
	public void testNewOk() {
		Assert.assertTrue(livesOk(new Block() {
			@Override
			protected void impl() {
				new Promise();
			}
		}));

		Assert.assertTrue(livesOk(new Block() {
			@Override
			protected void impl() {
				new Promise();
			}
		}));
	}

	@Test
	public void testInnerClassFunction() {
		Assert.assertTrue(livesOk(new Block() {
			@Override
			protected void impl() {
				final Promise.Function f = new Promise.Function() {
					@Override
					protected void impl(Object val) {
						Assert.assertNotNull(getNext());
					}
				};

				Assert.assertTrue(livesOk(new Block() {

					@Override
					protected void impl() {
						f.call(null);
					}
				}));

			}
		}));
	}

	@Test
	public void testPromiseWithFunction() {
		final Promise p = new Promise();

		Assert.assertTrue(livesOk(new Block() {

			@Override
			protected void impl() {
				p.run();
			}
		}));

		final Function fa = new Function() {

			@Override
			protected void impl(Object val) {
				Assert.assertNull(val);

				getNext().call("1");
			}
		};
		final Function fb = new Function() {

			@Override
			protected void impl(final Object val) {
				Assert.assertNotNull(val);
				Assert.assertTrue(val instanceof String);
				Assert.assertEquals("1", String.valueOf(val));

				CallbackModule c = new CallbackModule();
				c.callback(new CallbackModule.Callbackable() {

					@Override
					protected void impl(Object result) {
						getNext().call(String.valueOf(val) + String.valueOf(result));
					}
				});
			}
		};
		final Function fc = new Function() {

			@Override
			protected void impl(Object val) {
				Assert.assertNotNull(val);
				Assert.assertTrue(val instanceof String);
				Assert.assertEquals("1" + CallbackModule.RESULT_TEXT, String.valueOf(val));
			}
		};

		Assert.assertTrue(livesOk(new Block() {
			@Override
			protected void impl() {
				p.bind(fa).bind(fb, fc);
			}
		}));

		Assert.assertTrue(livesOk(new Block() {
			@Override
			protected void impl() {
				p.run();
			}
		}));

	}

	@Test
	public void testPromiseWithPromise() {
		final Promise p = new Promise();

		final Promise pa = getEmptyPromiseWith(new Function() {

			@Override
			protected void impl(Object val) {
				Assert.assertNull(val);

				getNext().call("1");
			}
		});
		final Promise pb = getEmptyPromiseWith(new Function() {

			@Override
			protected void impl(final Object val) {
				Assert.assertNotNull(val);
				Assert.assertTrue(val instanceof String);
				Assert.assertEquals("1", String.valueOf(val));

				CallbackModule c = new CallbackModule();
				c.callback(new CallbackModule.Callbackable() {

					@Override
					protected void impl(Object result) {
						getNext().call(String.valueOf(val) + String.valueOf(result));
					}
				});
			}
		});

		final Function fa = new Promise.Function() {

			@Override
			protected void impl(final Object val) {
				Assert.assertNotNull(val);
				Assert.assertTrue(val instanceof String);
				Assert.assertEquals("1" + CallbackModule.RESULT_TEXT, String.valueOf(val));


				CallbackModule c = new CallbackModule();
				c.callback(new CallbackModule.Callbackable() {

					@Override
					protected void impl(Object result) {
						getNext().call(String.valueOf(val) + String.valueOf(result));
					}
				});

			}
		};

		final Function fb = new Promise.Function() {

			@Override
			protected void impl(final Object val) {
				Assert.assertNotNull(val);
				Assert.assertTrue(val instanceof String);
				Assert.assertEquals("1" +
						CallbackModule.RESULT_TEXT +
						CallbackModule.RESULT_TEXT, String.valueOf(val));

				CallbackModule c = new CallbackModule();
				c.callback(new CallbackModule.Callbackable() {

					@Override
					protected void impl(Object result) {
						getNext().call(String.valueOf(val) + String.valueOf(result));
					}
				});

			}
		};

		pb.bind(fa, fb);

		final Promise pc = getEmptyPromiseWith(new Function() {

			@Override
			protected void impl(Object val) {
				Assert.assertNotNull(val);
				Assert.assertTrue(val instanceof String);
				Assert.assertEquals("1" +
						CallbackModule.RESULT_TEXT +
						CallbackModule.RESULT_TEXT +
						CallbackModule.RESULT_TEXT, String.valueOf(val));
			}
		});

		Assert.assertTrue(livesOk(new Block() {
			@Override
			protected void impl() {
				p.bind(pa).bind(pb, pc);
			}
		}));

		Assert.assertTrue(livesOk(new Block() {
			@Override
			protected void impl() {
				p.run();
			}
		}));

	}

	@Test
	public void testRunOnConcurrentEvent() {
		final ScheduledExecutorService worker =
				Executors.newScheduledThreadPool(3);

		Promise p = new Promise();

		final Promise pa = getEmptyPromiseWith(new Function() {

			@Override
			protected void impl(final Object val) {
				final Runnable ra = new Runnable() {

					public void run() {
						Assert.assertNull(val);
						getNext().call("1");
					}
				};

				worker.schedule(ra, 200, TimeUnit.MILLISECONDS);
			}
		});

		final Promise pb = getEmptyPromiseWith(new Function() {

			@Override
			protected void impl(final Object val) {
				final Runnable ra = new Runnable() {

					public void run() {
						Assert.assertNotNull(val);
						Assert.assertEquals("1", String.valueOf(val));
						getNext().call(String.valueOf(val).concat("2"));
					}
				};

				worker.schedule(ra, 100, TimeUnit.MILLISECONDS);
			}
		});

		final Promise pc = getEmptyPromiseWith(new Function() {

			@Override
			protected void impl(final Object val) {
				final Runnable ra = new Runnable() {

					public void run() {
						Assert.assertNotNull(val);
						Assert.assertEquals("12", String.valueOf(val));
						getNext().call(String.valueOf(val).concat("3"));
					}
				};

				worker.schedule(ra, 300, TimeUnit.MILLISECONDS);
			}
		});

		p.bind(pa, pb, pc).run();
	}

	@Test
	public void testRunWithArguments() {
		Promise p = new Promise();
		final Promise pa = getEmptyPromiseWith(new Function() {

			@Override
			protected void impl(Object val) {
				Assert.assertNotNull(val);
				Assert.assertEquals("hoge", String.valueOf(val));

				getNext().call(null);
			}
		});

		p.bind(pa).run("hoge");
	}

	public void testBreakChainIntentionally() {
		final Map<String, Integer> holder = new HashMap<String, Integer>();
		Promise p = new Promise();
		final Promise pa = getEmptyPromiseWith(new Function() {
			@Override
			protected void impl(Object val) {
				holder.put("increment", 0);

				getNext().call(null);
			}
		});

		final Promise pb = getEmptyPromiseWith(new Function() {

			@Override
			protected void impl(Object val) {
				holder.put("increment", 1);
				//getNext is not called here.
			}
		});

		final Promise pc = getEmptyPromiseWith(new Function() {

			@Override
			protected void impl(Object val) {
				// This promise must not be called.
				Assert.assertTrue(false);
			}
		});

		p.bind(pa, pb, pc).run();
		Assert.assertTrue(holder.get("increment") == 1);
	}


	private static Promise getEmptyPromiseWith(Function f) {
		return new Promise(f);
	}
}