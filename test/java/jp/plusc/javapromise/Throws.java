package jp.plusc.javapromise;

public class Throws {
	public static boolean livesOk(Block block) {
		try {
			block.invoke();
			return true;
		} catch (Throwable t) {
			t.printStackTrace();
			return false;
		}
	}

	public static boolean diesOk(Block block) {
		return !livesOk(block);
	}


	public static abstract class Block {
		public void invoke() {
			impl();
		}

		protected abstract void impl();
	}
}
