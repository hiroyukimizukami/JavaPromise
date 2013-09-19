package jp.plusc.javapromise;



/**
 * Promise enable to describe nested async-processes as pipeline processes.
 * @author hiroyukimizukami
 *
 */
public class Promise<T> {

	private Function<T> func = null;
	private Function<T> bottom = null;

	public Promise() {
		Function<T> pseudoFunction = getEmptyFunction();
		func = pseudoFunction;
		bottom = pseudoFunction;
	}

	public Promise(Function<T> f) {
		func = f;
		bottom = f;
	}

	public Promise<T> bind(Function<T> ...functions) {
		for (Function<T> f : functions) {
			bottom.n = f;
			bottom = bottom.n;
		}
		return this;
	}

	public Promise<T> bind(Promise<T> ...promises) {
		for (Promise<T> p : promises) {
			bottom.n = p.func;
			bottom = p.bottom;
		}
		return this;
	}

	public void run() {
		func.call(null);
	}

	public void run(T val) {
		func.call(val);
	}

	private final Function<T> getEmptyFunction() {
		return new Function<T>() {

			@Override
			protected void impl(T val) {
				next().call(val);
			}

		};
	}
}