package jp.plusc.javapromise;

/**
 * Promise enable to describe nested async-processes as pipeline processes.
 * @author hiroyukimizukami
 *
 */
public class Promise {

	private Function func = null;
	private Function bottom = null;

	public Promise() {
		Function pseudoFunction = getEmptyFunction();
		func = pseudoFunction;
		bottom = pseudoFunction;
	}

	public Promise(Function f) {
		func = f;
		bottom = f;
	}

	public Promise bind(Function ...functions) {
		for (Function f : functions) {
			bottom.next = f;
			bottom = bottom.next;
		}
		return this;
	}

	public Promise bind(Promise ...promises) {
		for (Promise p : promises) {
			bottom.next = p.func;
			bottom = p.bottom;
		}
		return this;
	}

	public void run() {
		func.call(null);
	}

	public void run(Object val) {
		func.call(val);
	}

	private static final Function getEmptyFunction() {
		return new Function() {

			@Override
			protected void impl(Object val) {
				getNext().call(val);
			}

		};
	}

	public static abstract class Function {
		Function next = null;

		public final void call(final Object val) {
			impl(val);
		}

		protected Function getNext() {
			if (next == null) {
				next = new Function () {

					@Override
					protected void impl(final Object val) {
						;
					}

				};
			}
			return next;
		}

		protected abstract void impl(final Object val);
	}
}
