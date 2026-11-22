INSERT INTO clients (name, phone_number, email, auth_subject)
VALUES (
    'client-01',
    '0000000000',
    'client-01@example.com',
    'dummy-idp-subject-client-01'
);

INSERT INTO accounts (id, name, balance, currency, client_id)
VALUES
    ('checking-001', 'Everyday Checking', 2450.75, 'USD',
        (SELECT id FROM clients WHERE auth_subject = 'dummy-idp-subject-client-01')),
    ('savings-001', 'Rainy Day Savings', 8200.00, 'USD',
        (SELECT id FROM clients WHERE auth_subject = 'dummy-idp-subject-client-01')),
    ('Octopus-001', 'Octopus Card', 150.00, 'HKD',
        (SELECT id FROM clients WHERE auth_subject = 'dummy-idp-subject-client-01')),
    ('credit-001', 'Visa Credit Card', -500.00, 'USD',
        (SELECT id FROM clients WHERE auth_subject = 'dummy-idp-subject-client-01'));

INSERT INTO transactions (account_id, description, amount, date, transaction_type, category)
VALUES
    ('credit-001', 'Grocery store', -64.28, DATE '2026-11-16', 'EXPENSE', NULL),
    ('credit-001', 'Paycheck', 1850.00, DATE '2026-11-15', 'INCOME', NULL),
    ('credit-001', 'Internet bill', -59.99, DATE '2026-11-12', 'EXPENSE', NULL),
    ('credit-001', 'Coffee shop', -4.50, DATE '2026-11-10', 'EXPENSE', NULL),
    ('savings-001', 'Salary', 2000.00, DATE '2026-11-10', 'INCOME', NULL),
    ('savings-001', 'Interest earned', 12.34, DATE '2026-11-01', 'INCOME', NULL),
    ('Octopus-001', 'Bus fare', -2.50, DATE '2026-11-05', 'EXPENSE', NULL),
    ('credit-001', 'Online shopping', -120.00, DATE '2026-11-08', 'EXPENSE', NULL),
    ('checking-001', 'Game', -20.00, DATE '2026-08-10', 'EXPENSE', 'VIDEO_GAMES'),
    ('checking-001', 'Game', -20.00, DATE '2026-09-10', 'EXPENSE', 'VIDEO_GAMES'),
    ('checking-001', 'Game', -20.00, DATE '2026-10-10', 'EXPENSE', 'VIDEO_GAMES'),
    ('checking-001', 'Game', -50.00, DATE '2026-11-10', 'EXPENSE', 'VIDEO_GAMES'),
    ('checking-001', 'Bar', -15.00, DATE '2026-08-15', 'EXPENSE', 'NIGHTLIFE'),
    ('checking-001', 'Bar', -15.00, DATE '2026-09-15', 'EXPENSE', 'NIGHTLIFE'),
    ('checking-001', 'Bar', -15.00, DATE '2026-10-15', 'EXPENSE', 'NIGHTLIFE'),
    ('checking-001', 'Bar', -40.00, DATE '2026-11-15', 'EXPENSE', 'NIGHTLIFE');
