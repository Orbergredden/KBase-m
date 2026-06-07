package mc.plugin.example;

import mc.plugins.api.DbEntry;
import mc.plugins.api.Plugin;
import mc.plugins.api.PluginContext;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * 
 */
public class examplePlugin implements Plugin {
    @Override
    public void run(PluginContext ctx) throws Exception {
        ctx.logger().info("examplePlugin: starting job...");

        //======== without connection
        DbEntry entry = ctx.connections().get("msgbase_dev_view");
        Connection conMsg = entry.conn();
        if (conMsg == null) {
            ctx.logger().severe("No msgbase_dev_view connection provided");
            return;
        }

        // демонстраційний запит
        try (PreparedStatement ps = conMsg.prepareStatement("select now() as ts");
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                String ts = rs.getString("ts");
                ctx.logger().info("msgBase DB time = " + ts);
            }
            rs.close();
        }
        
        //======== with connection
        DbEntry entry2 = ctx.connections().get("msgbase_dev_view");
        Connection conKBase = null;
        try {
        	conKBase = DriverManager.getConnection(entry2.url(), entry2.user(), entry2.password());
		} catch (Exception e) {
			ctx.logger().info("ERROR Некоректні дані, Помилка конекту до БД"+e.getMessage());
			e.printStackTrace();
		}

        // демонстраційний запит
        try (PreparedStatement ps = conKBase.prepareStatement("select now() as ts");
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                String ts = rs.getString("ts");
                ctx.logger().info("KBase DB time = " + ts);
            }
            rs.close();
        }
        
        conKBase.close();
        
        ctx.logger().info("examplePlugin: done.");
    }
}