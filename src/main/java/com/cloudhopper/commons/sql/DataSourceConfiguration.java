
package com.cloudhopper.commons.sql;

/**
 * Configuration of a DataSource to be used with a DataSourceManager.
 * @author joelauer
 */
public class DataSourceConfiguration implements Cloneable {

    private DataSourceProvider provider;
    private DatabaseVendor vendor;
    private String driver;
    private String name;
    private String url;
    private String username;
    private String password;
    private int minPoolSize;
    private int maxPoolSize;
    private boolean jmx;
    private String jmxDomain;
    private String validationQuery;
    // number of ms to wait for getConnection() to return -- defaults to 15000 ms
    private long checkoutTimeout;
    // should we check the connection on checkin/checkout?
    private boolean validateOnCheckout;
    private boolean validateOnCheckin;
    private long validateIdleConnectionTimeout;

    public DataSourceConfiguration() {
        // default provider is c3p0
        provider = DataSourceProvider.C3P0;
        // by default, jmx is turned on
        jmx = true;
        // by default, jmx domain is "com.cloudhopper"
        jmxDomain = "com.cloudhopper";
        // by default, pool size is initially 1 up to 10
        minPoolSize = 1;
        maxPoolSize = 10;
        // default SQL query is kind of lame
        validationQuery = null;
        checkoutTimeout = 15000;
        // validate on checkout/checkin is false by default
        validateOnCheckout = false;
        validateOnCheckin = false;
        // number of ms to test an idle connection - 0 means disabled
        validateIdleConnectionTimeout = 0;
    }

    /**
     * Sets whether the connection is validated on "checkout". Default value is
     * false. While this property guarantees a connection will be valid, it will
     * cause slow performance.
     * @param flag True to enable, otherwise false.
     */
    public void setValidateOnCheckout(boolean flag) {
        this.validateOnCheckout = flag;
    }

    /**
     * Gets whether the connection is validated on "checkout". Default value is
     * false. While this property guarantees a connection will be valid, it will
     * cause slow performance.
     * @return True if enabled, otherwise false.
     */
    public boolean getValidateOnCheckout() {
        return this.validateOnCheckout;
    }

    /**
     * Sets whether the connection is validated after "checkin". Default value is
     * false. Most providers will asynchronously validate the connection then
     * return it to the general pool.
     * @param flag True to enable, otherwise false.
     */
    public void setValidateOnCheckin(boolean flag) {
        this.validateOnCheckin = flag;
    }

    /**
     * Gets whether the connection is validated after "checkin". Default value is
     * false. Most providers will asynchronously validate the connection then
     * return it to the general pool.
     * @return True if enabled, otherwise false.
     */
    public boolean getValidateOnCheckin() {
        return this.validateOnCheckin;
    }

    /**
     * If supported by the provider, sets the amount of time (in milliseconds)
     * to wait before validating connections that are idle.  A value of zero
     * disables this functionality.  Default value is zero.
     * @param ms The number of milliseconds to wait before validating an idle
     *      connection.
     * @throws SQLConfigurationException Thrown if the value is < 0
     */
    public void setValidateIdleConnectionTimeout(long ms) throws SQLConfigurationException {
        if (ms < 0) {
            throw new SQLConfigurationException("Value must be >= 0");
        }
        this.validateIdleConnectionTimeout = ms;
    }

    /**
     * Gets the amount of time (in milliseconds) to wait before validating
     * an idle connection. A value of zero indicates this is disabled.  Default
     * value is zero.
     * @return The number of milliseconds to wait before idle connections are
     *      validated.
     */
    public long getValidateIdleConnectionTimeout() {
        return this.validateIdleConnectionTimeout;
    }

    /**
     * Sets the underlying provider of the DataSource such as BASIC (no pooling)
     * or something like C3P0 which is a pooling provider.  The provider determines
     * which functionality will be available.
     * @param provider The underlying DataSource provider such as C3P0
     */
    public void setProvider(DataSourceProvider provider) {
        this.provider = provider;
    }

    /**
     * Gets the underlying DataSource provider.
     * @return The underlying DataSource provider.
     */
    public DataSourceProvider getProvider() {
        return this.provider;
    }

    /**
     * Sets the database vendor associated with this datasource.  For example,
     * connecting to a MySQL database would have this property set to MYSQL.
     * Setting the vendor (either directly or indirectly by setting the URL),
     * will also automatically set defaults for the database driver and default
     * validation query to use.
     * @param vendor The database vendor such as MYSQL
     */
    public void setVendor(DatabaseVendor vendor) {
        // attempt to automatically determine the database driver if it hasn't been set
        setDriverIfNotSet(vendor.getDefaultDriver());
        // attempt to automatically determine validation query if it hasn't been set
        setValidationQueryIfNotSet(vendor.getDefaultValidationQuery());
        // set the database vendor
        this.vendor = vendor;
    }

    /**
     * Internal method to set the database vendor if it hasn't already been set.
     */
    private void setVendorIfNotSet(DatabaseVendor vendor) {
        if (this.vendor == null) {
            setVendor(vendor);
        }
    }

    /**
     * Gets the database vendor associated with this DataSource. For example,
     * connecting to a MySQL database would have this property set to MYSQL.
     * @return The database vendor
     */
    public DatabaseVendor getVendor() {
        return this.vendor;
    }

    /**
     * Sets the SQL query to use when validating a connection.  This property
     * is automatically set to defaults by setting the vendor or the URL.
     * @param query The SQL query to use when validating a connection
     */
    public void setValidationQuery(String query) {
        this.validationQuery = query;
    }

    /**
     * Internal method to set the validation query if its not already set.
     */
    private void setValidationQueryIfNotSet(String query) {
        if (this.validationQuery == null) {
            setValidationQuery(query);
        }
    }

    /**
     * Gets the SQL query to use when validating a connection.  This property
     * is automatically set to defaults by setting the vendor or the URL.
     * @return The SQL query to use when validating a connection
     */
    public String getValidationQuery() {
        return this.validationQuery;
    }

    /**
     * Sets the name of this DataSource such as "main" or "dbname".  This name
     * is used by a provider for naming a DataSource.  For example, this is the
     * name used for registering this DataSource via JMX and logging statements.
     * @param name The name associated with this DataSource
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the name of this DataSource such as "main" or "dbname".  This name
     * is used by a provider for naming a DataSource.  For example, this is the
     * name used for registering this DataSource via JMX and logging statements.
     * @param name The name associated with this DataSource
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the JDBC url to connect to the database. In JDBC, all url's begin
     * with jdbc:protocol: This is the standard. After this is driver specific,
     * and no two drivers are the same. This method will partially parse the
     * URL to extract out various properties to assist in configuring this factory.
     * 
     * HSQL: jdbc:hsqldb:mem:
     *      jdbc:hsqldb:file:
     * 
     * MySQL: jdbc:mysql://[host:port],[host:port].../[database][?propertyName1][=propertyValue1][&propertyName2][=propertyValue2]
     *      jdbc:mysql://localhost:3306/stratus001?useTimezone=true&useLegacyDatetimeCode=false&serverTimezone=UTC
     *
     * PostgreSQL: jdbc:postgresql:database
     *      jdbc:postgresql://host/database
     *      jdbc:postgresql://host:port/database
     *      jdbc:postgresql://localhost:5432/testdb?charSet=LATIN1
     *
     * JTDS: jdbc:jtds:<server_type>://<server>[:<port>][/<database>][;<property>=<value>[;...]]
     *      jdbc:jtds:sqlserver://localhost/dbname
     *      jdbc:jtds:sybase://localhost/dbname
     *
     * Microsoft: jdbc:sqlserver://[serverName[\instanceName][:portNumber]][;property=value[;property=value]]
     *      jdbc:sqlserver://localhost;databaseName=AdventureWorks;integratedSecurity=true;applicationName=MyApp;
     *      jdbc:sqlserver://localhost:1433;instanceName=instance1;databaseName=AdventureWorks;integratedSecurity=true;applicationName=MyApp;
     *
     * @param url
     * @throws com.cloudhopper.commons.sql.SQLConfigurationException
     */
    public void setUrl(String url) throws SQLConfigurationException {
        // make sure the url starts with "jdbc:"
        if (!url.startsWith("jdbc:")) {
            throw new SQLConfigurationException("Invalid JDBC URL. Does not start with 'jdbc:'");
        }

        // attempt to parse the protocol from the JDBC url -- this is the portion
        // after the initial "jdbc:protocol:" -- try to find the next ':' char after the first 5
        int protocolPos = url.indexOf(":", 5);
        if (protocolPos <= 0) {
            throw new SQLConfigurationException("Invalid JDBC URL. Does not start with a protocol that ends in ':' such as 'jdbc:protocol:'");
        }
        String protocol = url.substring(5, protocolPos);

        // attempt to parse the next protocol token
        int subProtocolPos = url.indexOf(":", protocolPos+1);
        String subProtocol = "";
        if (subProtocolPos > 0) {
            subProtocol = url.substring(protocolPos+1, subProtocolPos);
        }

        // attempt to map JDBC url to set the vendor -- this will set the vendor
        // and also the driver if those properties have not yet been set
        if (protocol.equals("mysql")) {
            setVendorIfNotSet(DatabaseVendor.MYSQL);
        } else if (protocol.equals("jtds") && subProtocol.equals("sqlserver")) {
            setVendorIfNotSet(DatabaseVendor.MSSQL);
        } else {
            throw new SQLConfigurationException("Unsupported protocol '" + protocol + (!subProtocol.equals("") ? ":" + subProtocol : "") + "' in JDBC URL. Add mapping to DataSourceFactory?");
        }

        // everything went ok, save the url
        this.url = url;
    }

    /**
     * Gets the JDBC URL used when connecting to the database.
     * @return The JDBC URL
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * Sets the database driver class to use for connecting to the database.
     * This property is automatically set either by setting the vendor or by
     * setting the JDBC URL property.
     * @param driver The database driver class
     */
    public void setDriver(String driver) {
        this.driver = driver;
    }

    /**
     * Gets the database driver class to use for connecting to the database.
     * This property is automatically set either by setting the vendor or by
     * setting the JDBC URL property.
     * @return The database driver class
     */
    public String getDriver() {
        return this.driver;
    }

    /**
     * Internal method to set the database driver if it hasn't already been set.
     */
    private void setDriverIfNotSet(String driver) {
        if (this.driver == null) {
            setDriver(driver);
        }
    }

    /**
     * Sets the username to use for connecting to the DataSource.
     * @param username The username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the username to use for connecting to the DataSource.
     * @return The username
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Sets the password to use for connecting to the DataSource.
     * @param password The password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the password to use for connecting to the DataSource.
     * @return The password
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * If supported by the provider, sets if this DataSource should be
     * registered as an MBean in a JMX server.
     * @param jmx True to enable, otherwise false.
     */
    public void setJmx(boolean jmx) {
        this.jmx = jmx;
    }

    /**
     * If supported by the provider, gets if this DataSource should be
     * registered as an MBean in a JMX server.
     * @return True if enabled, otherwise false.
     */
    public boolean getJmx() {
        return this.jmx;
    }

    /**
     * Sets the domain to use for registering this connection to a JMX MBean
     * server. The default value is "com.cloudhopper"
     * @param domain The domain to use for registering this DataSource
     */
    public void setJmxDomain(String domain) {
        this.jmxDomain = domain;
    }

    /**
     * Gets the domain to use for registering this connection to a JMX MBean
     * server. The default value is "com.cloudhopper"
     * @return The domain to use for registering this DataSource
     */
    public String getJmxDomain() {
        return this.jmxDomain;
    }

    /**
     * If the provider supports connection pooling, sets the minimum number of
     * connections to have in the pool.  The default value is 1.
     * @param size The minimum number of connections to pool
     * @throws SQLConfigurationException Thrown if the value is <= 0
     */
    public void setMinPoolSize(int size) throws SQLConfigurationException {
        // must be > 0
        if (size <= 0) {
            throw new SQLConfigurationException("Value must be > 0");
        }
        this.minPoolSize = size;
    }

    /**
     * Gets the minimum number of connections to have in the pool.  The default
     * value is 1.
     * @return The minimum number of connections to pool
     */
    public int getMinPoolSize() {
        return this.minPoolSize;
    }

    /**
     * If the provider supports connection pooling, sets the maximum number of
     * connections to have in the pool.  The default value is 10.
     * @param size The maximum number of connections to pool
     * @throws SQLConfigurationException Thrown if the value is <= 0
     */
    public void setMaxPoolSize(int size) throws SQLConfigurationException {
        // must be > 0
        if (size <= 0) {
            throw new SQLConfigurationException("Value must be > 0");
        }
        this.maxPoolSize = size;
    }

    /**
     * Gets the maximum number of connections to have in the pool.  The default
     * value is 10.
     * @return The maximum number of connections to pool
     */
    public int getMaxPoolSize() {
        return this.maxPoolSize;
    }

    /**
     * If supported by the provider, sets the number of milliseconds a client
     * calling getConnection() will wait for a Connection to be checked-in or
     * acquired when the pool is exhausted.
     * Zero means wait indefinitely. Setting any positive value will cause the
     * getConnection() call to timeout and break with an SQLException after the
     * specified number of milliseconds. Default value is 15000 (15 seconds).
     * @param ms The number of milliseconds
     * @throws SQLConfigurationException Thrown if the value is < 0
     */
    public void setCheckoutTimeout(long ms) throws SQLConfigurationException {
        if (ms < 0) {
            throw new SQLConfigurationException("Value must be >= 0");
        }
        this.checkoutTimeout = ms;
    }

    /**
     * Gets the number of milliseconds a client calling getConnection() will wait
     * for a Connection to be checked-in or acquired when the pool is exhausted.
     * Zero means wait indefinitely. Setting any positive value will cause the
     * getConnection() call to timeout and break with an SQLException after the
     * specified number of milliseconds. Default value is 15000 (15 seconds).
     * @return The number of milliseconds
     */
    public long getCheckoutTimeout() {
        return this.checkoutTimeout;
    }
    
    @Override
    public String toString() {
        return new StringBuilder(200)
                .append("[name=")
                .append(this.name)
                .append(", provider=")
                .append(this.provider)
                .append(", vendor=")
                .append(this.vendor)
                .append(", driver=")
                .append(this.driver)
                .append(", url={")
                .append(this.url)
                .append("}, username=")
                .append(this.username)
                .append(", password=")
                .append((this.username == null ? "null" : "*****"))
                .append("]")
                .toString();
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }

}
