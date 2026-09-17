import psycopg2
from psycopg2 import sql
import os
from dotenv import load_dotenv

# Load environment variables from .env file
load_dotenv()

def connect_to_db():
    try:
        connection = psycopg2.connect(
            host=os.getenv("DB_HOST", "localhost"),
            port=os.getenv("DB_PORT", 5432),
            database=os.getenv("DB_NAME"),
            user=os.getenv("DB_USER"),
            password=os.getenv("DB_PASSWORD")
        )
        print("Connected to PostgreSQL database successfully")
        return connection
    except psycopg2.Error as e:
        print(f"Unable to connect to the database: {e}")
        return None


def close_connection(connection):
    """Close the database connection."""
    if connection:
        connection.close()
        print("Database connection closed")

# Example usage
if __name__ == "__main__":
    conn = connect_to_db()
    if conn:
        close_connection(conn)

