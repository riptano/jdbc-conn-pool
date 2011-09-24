package me.prettyprint.cassandra.jdbc;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;
import javax.sql.DataSource;

import me.prettyprint.cassandra.connection.CassandraHostConfigurator;
import me.prettyprint.cassandra.connection.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.factory.HFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A factory for JNDI Resource managed objects. Responsible for creating a
 * {@link Keyspace} references for passing to {@link HFactory}. A limited set of
 * configuration parameters are supported. These parameters are defined in a web
 * application's context.xml file. Parameter descriptions can be found in
 * {@link CassandraHostConfigurator}
 * 
 * <p>
 * 
 * <pre>
 *     <Resource name="cassandra/CassandraClientFactory"
 *               auth="Container"
 *               type="me.prettyprint.cassandra.api.Keyspace"
 *               factory="me.prettyprint.cassandra.jndi.CassandraClientJndiResourceFactory"
 *               hosts="cass1:9160,cass2:9160,cass3:9160"
 *               user="user"
 *               password="passwd"
 *               keyspace="Keyspace1"
 *               clusterName="Test Cluster" 
 *               maxActive="20"
 *               maxWaitTimeWhenExhausted="10"
 *               autoDiscoverHosts="true"
 *               runAutoDiscoveryAtStartup="true"/>
 * </pre>
 *
 */

public class HCQLDataSource implements DataSource, ObjectFactory {

    /** Serialization UID. */
    private static final long serialVersionUID = -1561804548443209469L;

    private Logger log = LoggerFactory.getLogger(HCQLDataSource.class);

    private volatile boolean initialized = false;
    
    private Cluster cluster;

    private CassandraHostConfigurator cassandraHostConfigurator;
    
    private HCQLDataSource instance;

    /** Config setting. */
    private PrintWriter logWriter = null;

    private Keyspace keyspace;

    public HCQLDataSource() {
       
    }

    /**
     * Sets the maximum time in seconds that this data source will wait while
     * attempting to connect to a database. A value of zero specifies that the
     * timeout is the default system timeout if there is one; otherwise, it
     * specifies that there is no timeout. When a DataSource object is created,
     * the login timeout is initially zero.
     */
    public void setLoginTimeout(int seconds) throws SQLException {
        throw new UnsupportedOperationException("setLoginTimeout is unsupported.");
    }

    /**
     * Gets the maximum time in seconds that this data source can wait while
     * attempting to connect to a database. A value of zero means that the
     * timeout is the default system timeout if there is one; otherwise, it
     * means that there is no timeout. When a DataSource object is created, the
     * login timeout is initially zero.
     * 
     */
    public int getLoginTimeout() throws SQLException {
        throw new UnsupportedOperationException("getLoginTimeout is unsupported.");
    }

    /**
     * Returns true if this either implements the interface argument or is
     * directly or indirectly a wrapper for an object that does.
     * 
     * @param arg0
     *            class
     * @return t/f
     * @throws SQLException
     *             on error
     * 
     */
    public boolean isWrapperFor(Class<?> arg0) throws SQLException {
        return false;
    }

    /**
     * Retrieves the log writer for this DataSource object.
     * 
     */
    public PrintWriter getLogWriter() throws SQLException {
        return this.logWriter;
    }

    /**
     * Returns an object that implements the given interface to allow access to
     * non-standard methods, or standard methods not exposed by the proxy.
     * 
     * @param arg0
     *            obj
     * @return unwrapped object
     * @throws SQLException
     */
    @SuppressWarnings("all")
    public Object unwrap(Class arg0) throws SQLException {
        return null;
    }

    /**
     * Sets the log writer for this DataSource object to the given
     * java.io.PrintWriter object.
     */
    public void setLogWriter(PrintWriter out) throws SQLException {
        this.logWriter = out;
    }

    /**
     * {@inheritDoc}
     * 
     * @see javax.sql.DataSource#getConnection()
     */
    public Connection getConnection() throws SQLException {
        return this.cluster.getConnectionManager().;
        //return null;
    }

    /**
     * Close the datasource.
     * 
     */
    public void close() {
        HFactory.shutdownCluster(cluster);
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.sql.DataSource#getConnection(java.lang.String,
     *      java.lang.String)
     */
    public Connection getConnection(String username, String password) throws SQLException {
        //return this.multiDataSource.get(new UsernamePassword(username, password)).getConnection();
        return null;
    }

    /**
     * Creates an object using the location or reference information specified.
     * 
     * @param object
     *            The possibly null object containing location or reference
     *            information that can be used in creating an object.
     * @param jndiName
     *            The name of this object relative to nameCtx, or null if no
     *            name is specified.
     * @param context
     *            The context relative to which the name parameter is specified,
     *            or null if name is relative to the default initial context.
     * @param environment
     *            The possibly null environment that is used in creating the
     *            object.
     * 
     * @return Object - The object created; null if an object cannot be created.
     * 
     * @exception Exception
     *                - if this object factory encountered an exception while
     *                attempting to create an object, and no other object
     *                factories are to be tried.
     */
    public Object getObjectInstance(Object object, Name jndiName, Context context, Hashtable<?, ?> environment)
            throws Exception {
        Reference resourceRef = null;

        if (object instanceof Reference) {
            resourceRef = (Reference) object;
        } else {
            throw new Exception("Object provided is not a javax.naming.Reference type");
        }

        // config CassandraHostConfigurator
        synchronized (this) {
            if (!initialized) {
                configure(resourceRef);
                instance = new HCQLDataSource();
            }
        }

        return instance;
    }

    private void configure(Reference resourceRef) throws Exception {
        // required
        RefAddr hostsRefAddr = resourceRef.get("hosts");
        RefAddr clusterNameRef = resourceRef.get("clusterName");
        RefAddr keyspaceNameRef = resourceRef.get("keyspace");
        RefAddr userRef = resourceRef.get("user");
        RefAddr passwordRef = resourceRef.get("password");

        // optional
        RefAddr maxActiveRefAddr = resourceRef.get("maxActive");
        RefAddr maxWaitTimeWhenExhausted = resourceRef.get("maxWaitTimeWhenExhausted");
        RefAddr autoDiscoverHosts = resourceRef.get("autoDiscoverHosts");
        RefAddr runAutoDiscoverAtStartup = resourceRef.get("runAutoDiscoveryAtStartup");
        RefAddr retryDownedHostDelayInSeconds = resourceRef.get("retryDownedHostDelayInSeconds");

        if (hostsRefAddr == null || hostsRefAddr.getContent() == null) {
            throw new Exception("A url and port on which Cassandra is installed and listening "
                    + "on must be provided as a ResourceParams in the context.xml");
        }

        cassandraHostConfigurator = new CassandraHostConfigurator((String) hostsRefAddr.getContent());
        if (autoDiscoverHosts != null) {
            cassandraHostConfigurator
                    .setAutoDiscoverHosts(Boolean.parseBoolean((String) autoDiscoverHosts.getContent()));
            if (runAutoDiscoverAtStartup != null)
                cassandraHostConfigurator.setRunAutoDiscoveryAtStartup(Boolean.parseBoolean((String) autoDiscoverHosts
                        .getContent()));
        }
        if (retryDownedHostDelayInSeconds != null) {
            int retryDelay = Integer.parseInt((String) retryDownedHostDelayInSeconds.getContent());
            // disable retry if less than 1
            if (retryDelay < 1)
                cassandraHostConfigurator.setRetryDownedHosts(false);
            cassandraHostConfigurator.setRetryDownedHostsDelayInSeconds(retryDelay);
        }
        if (maxActiveRefAddr != null)
            cassandraHostConfigurator.setMaxActive(Integer.parseInt((String) maxActiveRefAddr.getContent()));
        if (maxWaitTimeWhenExhausted != null)
            cassandraHostConfigurator.setMaxWaitTimeWhenExhausted(Integer.parseInt((String) maxWaitTimeWhenExhausted
                    .getContent()));

        if (log.isDebugEnabled())
            log.debug("JNDI resource created with CassandraHostConfiguration: {}",
                    cassandraHostConfigurator.getAutoDiscoverHosts());
        
        cassandraHostConfigurator.setUser((String) userRef.getContent());
        cassandraHostConfigurator.setPassword((String) passwordRef.getContent());
        cassandraHostConfigurator.setKeyspaceName((String) keyspaceNameRef.getContent());

        cluster = HFactory.createCluster((String) clusterNameRef.getContent(), cassandraHostConfigurator);
        keyspace = HFactory.createKeyspace((String) keyspaceNameRef.getContent(), cluster);
        initialized = true;
    }

}
