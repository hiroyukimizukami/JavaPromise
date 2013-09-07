package jp.plusc.javapromise;

public class CallbackModule {
	public static final String RESULT_TEXT = Callbackable.class.getSimpleName();
	public void callback(Callbackable c) {
		c.call();
	}

	public static abstract class Callbackable {
		public void call() {
			impl(RESULT_TEXT);
		}

		protected abstract void impl(Object result);
	}

}
