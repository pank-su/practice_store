CREATE TABLE IF NOT EXISTS users (
  id SERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL CHECK (length(trim(name)) > 0),
  email VARCHAR(255) UNIQUE NOT NULL CHECK (length(trim(email)) > 0),
  age INT NOT NULL CHECK (age > 0),
  password_hash VARCHAR(255) NOT NULL CHECK (length(trim(password_hash)) > 0)
);

CREATE TABLE IF NOT EXISTS orders (
  id SERIAL PRIMARY KEY,
  user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  product VARCHAR(255) NOT NULL CHECK (length(trim(product)) > 0),
  quantity INT NOT NULL CHECK (quantity > 0),
  price DECIMAL(10, 2) NOT NULL CHECK (price > 0),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders(user_id);

CREATE OR REPLACE FUNCTION create_user(
  p_name VARCHAR,
  p_email VARCHAR,
  p_age INT,
  p_password_hash VARCHAR
)
RETURNS users
LANGUAGE plpgsql
AS $$
DECLARE
  created_user users;
BEGIN
  INSERT INTO users (name, email, age, password_hash)
  VALUES (trim(p_name), lower(trim(p_email)), p_age, p_password_hash)
  RETURNING * INTO created_user;

  RETURN created_user;
END;
$$;
