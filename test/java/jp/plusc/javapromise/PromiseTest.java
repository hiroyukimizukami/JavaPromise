package jp.plusc.javapromise;

import static jp.plusc.javapromise.Throws.livesOk;
import jp.plusc.javapromise.Promise.Function;
import jp.plusc.javapromise.Throws.Block;

import org.junit.Assert;
import org.junit.Test;

public class PromiseTest {

	@Test
	public void testNew() {
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
		pb.bind(fa);

		final Promise pc = getEmptyPromiseWith(new Function() {

			@Override
			protected void impl(Object val) {
				Assert.assertNotNull(val);
				Assert.assertTrue(val instanceof String);
				Assert.assertEquals("1" +
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

	private static Promise getEmptyPromiseWith(Function f) {
		return new Promise(f);
	}
}