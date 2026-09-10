# The business must be able to analyse trading activity over time — by instrument,by period, by client segment.
# The platform should surface a small set of business insights (for example, trading volumes, most active instruments, client activity trends) to internal stakeholders

from db_connection import connect_to_db, close_connection
import pandas as pd
import matplotlib.pyplot as plt



def analyze_trading_activity_by_instrument(connection):

    cursor = None
    
    try:
        cursor = connection.cursor()
        query = """
            SELECT
                i.instrument_id,
                i.ticker,
                i.name,
                i.asset_class,
                COUNT(o.order_id) as total_orders,
                COUNT(CASE WHEN o.order_type = 'BUY' THEN 1 END) as buy_orders,
                COUNT(CASE WHEN o.order_type = 'SELL' THEN 1 END) as sell_orders,
                SUM(o.quantity) as total_volume,
                AVG(o.quantity) as avg_quantity,
                COUNT(CASE WHEN o.status = 'FILLED' THEN 1 END) as filled_orders,
                COUNT(CASE WHEN o.status = 'CANCELLED' THEN 1 END) as cancelled_orders,
                AVG(o.limit_price) as avg_limit_price
            FROM instruments i
            LEFT JOIN orders o ON i.instrument_id = o.instrument_id
            GROUP BY i.instrument_id, i.ticker, i.name, i.asset_class
            ORDER BY total_volume DESC NULLS LAST
        """
        cursor.execute(query)
        results = cursor.fetchall()
        
        # Get column names from cursor description
        column_names = [desc[0] for desc in cursor.description]
        
        # Convert to pandas DataFrame
        df = pd.DataFrame(results, columns=column_names)
        return df
        
    except Exception as e:
        print(f"Error analyzing trading activity by instrument: {e}")
        return None
    
    finally:
        if cursor:
            cursor.close()


def visualize_trading_activity_by_instrument(df):

    if df is None or df.empty:
        print("No data to visualize")
        return
    
    # Filter out rows with no trading activity
    df_active = df[df['total_orders'] > 0].copy()
    
    if df_active.empty:
        print("No active trading data to visualize")
        return
    
    # Create figure with multiple subplots
    fig, axes = plt.subplots(2, 2, figsize=(14, 10))
    fig.suptitle('Trading Activity Analysis by Instrument', fontsize=16, fontweight='bold')
    
    # Subplot 1: Total Volume by Instrument
    ax1 = axes[0, 0]
    ax1.barh(df_active['ticker'], df_active['total_volume'], color='steelblue')
    ax1.set_xlabel('Total Volume')
    ax1.set_title('Total Trading Volume by Instrument')
    ax1.invert_yaxis()
    
    # Subplot 2: Buy vs Sell Orders
    ax2 = axes[0, 1]
    x = range(len(df_active))
    width = 0.35
    ax2.bar([i - width/2 for i in x], df_active['buy_orders'], width, label='Buy Orders', color='green')
    ax2.bar([i + width/2 for i in x], df_active['sell_orders'], width, label='Sell Orders', color='red')
    ax2.set_xticks(x)
    ax2.set_xticklabels(df_active['ticker'], rotation=45, ha='right')
    ax2.set_ylabel('Number of Orders')
    ax2.set_title('Buy vs Sell Orders by Instrument')
    ax2.legend()
    
    # Subplot 3: Order Status Distribution
    ax3 = axes[1, 0]
    ax3.barh(df_active['ticker'], df_active['filled_orders'], label='Filled', color='lightgreen')
    ax3.barh(df_active['ticker'], df_active['cancelled_orders'], left=df_active['filled_orders'], 
             label='Cancelled', color='lightcoral')
    ax3.set_xlabel('Number of Orders')
    ax3.set_title('Order Status Distribution by Instrument')
    ax3.legend()
    ax3.invert_yaxis()
    
    # Subplot 4: Average Limit Price
    ax4 = axes[1, 1]
    ax4.bar(df_active['ticker'], df_active['avg_limit_price'], color='orange')
    ax4.set_ylabel('Average Limit Price')
    ax4.set_title('Average Limit Price by Instrument')
    ax4.tick_params(axis='x', rotation=45)
    
    plt.tight_layout()
    plt.show()

    pass


def analyze_trading_activity_by_client_segment(connection):
    """
    Analyze trading activity grouped by client segment.
    
    Returns:
        Analysis results by client segment
    """
    pass


def get_trading_volumes(connection):
    """
    Retrieve trading volume insights.
      
    Returns:
        Trading volume metrics
    """
    pass


def get_most_active_instruments(connection):
    """
    Retrieve the most active trading instruments.
       
    Returns:
        List of most active instruments with metrics
    """
    pass


def get_client_activity_trends(connection):
    pass




conn = connect_to_db()
df = analyze_trading_activity_by_instrument(conn)
visualize_trading_activity_by_instrument(df)
close_connection(conn)
  