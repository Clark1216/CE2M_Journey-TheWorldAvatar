package cam.dev.zhouxiaochi;

public class EntityInfo {

		private String name;
		public EntityInfo(String name, String owlSource) {
			super();
			this.name = name;
			this.owlSource = owlSource;
		}
		private String owlSource;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getOwlSource() {
			return owlSource;
		}
		public void setOwlSource(String owlSource) {
			this.owlSource = owlSource;
		}



	}

