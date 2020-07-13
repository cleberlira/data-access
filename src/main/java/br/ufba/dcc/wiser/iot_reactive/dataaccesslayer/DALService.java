/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufba.dcc.wiser.iot_reactive.dataaccesslayer;

import br.ufba.dcc.wiser.iot_reactive.dataaccesslayer.connect.Connect;
import br.ufba.dcc.wiser.iot_reactive.dataaccesslayer.util.Runner;
import br.ufba.dcc.wiser.iot_reactive.model.Device;
import br.ufba.dcc.wiser.iot_reactive.model.SensorData;
import io.vertx.core.AbstractVerticle;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.SqlConnection;
import java.sql.Timestamp;
import java.util.List;

/**
 *
 * @author cleberlira
 */
public class DALService extends AbstractVerticle {

    public static void main(String[] args) {
        Runner.runExample(Connect.class);
    }

    
    @Override
    public void start() {
     createTables();
    }

    private void storeSensorData(List<SensorData> listSensorData, Device device) {
        Connect connect = new Connect();

        try {

            MySQLPool client = connect.connect();

            client.getConnection(conn -> {
                if (conn.failed()) {

                    System.out.println("erro " + conn.cause().getMessage());
                    conn.cause().printStackTrace();;

                    return;
                }

                SqlConnection connection = conn.result();

                for (SensorData sensorData : listSensorData) {

                    String sensorId = sensorData.getSensor().getId();

                    Timestamp startDateTime = new Timestamp(sensorData.getStartTime().getTime());
                    Timestamp endDateTime = new Timestamp(sensorData.getEndTime().getTime());

                    connection.query("INSERT INTO sensor_data (sensor_id, device_id, data_value, start_datetime, end_datetime) values " + "('" + sensorId + "', '" + device.getId() + "', '" + sensorData.getValue() + "' ,'" + startDateTime + "', '" + endDateTime + "')")
                            .execute(res -> {
                                if (res.failed()) {
                                    System.out.println("erro ao inserir em sensor_data");
                                    throw new RuntimeException(res.cause());

                                }

                                if (res.succeeded()) {
                                    System.out.println("data inserted with sucessful in sensor_data");
                                }

                              
                                connection.close();

                            });

                }
            });
        } catch (Exception ex) {
            System.out.println("erro de conexão");
        }

    }
    
    
    private void createTables() {
        Connect connect = new Connect();

        MySQLPool client = connect.connect();

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
