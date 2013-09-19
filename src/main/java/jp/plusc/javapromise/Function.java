package jp.plusc.javapromise;

public abstract class Function<T> {
	Function<T> n = null;

	public final void call(final T val) {
		impl(val);
	}

	protected Function<T> next() {
		if (n == null) {
			n = new Function<T> () {

				@Override
				protected void impl(final T val) {
					;
				}

			};
		}
		return n;
	}

	protected abstract void impl(final T val);
}
