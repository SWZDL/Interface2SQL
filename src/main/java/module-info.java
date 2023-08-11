module cn.shengwenzeng.instanceinterfacesqlfx {
	requires javafx.controls;
	requires javafx.fxml;

	requires org.controlsfx.controls;
	requires com.dlsc.formsfx;
	requires org.kordamp.bootstrapfx.core;
	requires org.apache.poi.poi;
	requires org.apache.poi.ooxml;

	opens cn.shengwenzeng.instanceinterfacesqlfx to javafx.fxml;
	exports cn.shengwenzeng.instanceinterfacesqlfx;
}