/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufba.dcc.wiser.iot_reactive.dataaccesslayer.connect;

import br.ufba.dcc.wiser.iot_reactive.dataaccesslayer.util.Runner;
import io.vertx.core.AbstractVerticle;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnection;

/**
 *
 * @author cleberlira
 */
public class Connect extends AbstractVerticle {

   
    public static void main(String[] args) {
        Runner.runExample(Connect.class);
    }

    public MySQLPool connect() {

        MySQLPool client = null;
        MySQLConnectOptions connectOptions = new MySQLConnectOptions()
                .setPort(3306)
                .setHost("localhost")
                .setDatabase("iotDB")
                .setUser("iotreactive")
                .setPassword("1234");

        PoolOptions poolOptions = new PoolOptions()
                .setMaxSize(5);

        client = MySQLPool.pool(vertx, connectOptions, poolOptions);

        return client;

    }

    private void createTables() {

        MySQLPool client = connect();

        client.getConnection(ar1 -> {

        if (ar1.succeeded()) {

            System.out.println("Connected");

            // Obtain our connection
            SqlConnection conn = ar1.result();
            // All operations execute on the same connection
            conn
                    .query("CREATE TABLE IF NOT EXISTS sensor_data(ID BIGINT AUTO_INCREMENT PRIMARY KEY, sensor_id VARCHAR(255), device_id VARCHAR(255), data_value VARCHAR(255), start_datetime TIMESTAMP, end_datetime TIMESTAMP, aggregation_status INT DEFAULT 0)")
                    .execute(res -> {

                        if (res.failed()) {
                            System.out.println("falhou sensor_data");

                            throw new RuntimeException(res.cause());
                        }
                        if (res.succeeded()) {

                            System.out.println("table sensor_data created with sucessfull");
                        }

                        conn
                                .query("CREATE TABLE IF NOT EXISTS semantic_registered_last_time_sensors(sensor_id VARCHAR(255), device_id VARCHAR(255), last_time TIMESTAMP)")
                                .execute(res2 -> {
                                    if (res2.failed()) {
                                        System.out.println("falhou semantic_registered_last_time_sensors");
                                        throw new RuntimeException(res2.cause());
                                    }

                                    if (res.succeeded()) {
                                        System.out.println("table semantic_registered_last_time_sensors created with sucessfull");
                                    }

                                    conn
                                            .query("CREATE TABLE IF NOT EXISTS aggregation_registered_last_time_sensors(sensor_id VARCHAR(255),device_id VARCHAR(255), last_time TIMESTAMP)")
                                            .execute(res3 -> {

                                                if (res3.failed()) {
                                                    System.out.println("falhou aggregation_registered_last_time_sensors");
                                                    throw new RuntimeException(res3.cause());
                                                }

                                                if (res.succeeded()) {
                                                    System.out.println("table aggregation_registered_last_time_sensors created with sucessfull");
                                                }

                                                // Release the connection to the pool
                                                conn.close();
                                            });
                                });
                    });

        } else {
            System.out.println("Could not connect: " + ar1.cause().getMessage());

        }
        });

    }

}
