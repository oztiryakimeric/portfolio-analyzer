# Portfolio Analyzer 🛟

Portfolio Analyzer is an open-source Java project that enables you to generate detailed Excel reports for analyzing financial portfolios. Whether you're managing investments in the stock market, mutual funds, or precious metals, this project provides comprehensive analytics to help you make informed decisions.

- Check the example report: [example-report.xlsx](https://docs.google.com/spreadsheets/d/12m0i6IegBx6-dQrmbd2Bu1LXfq0dnfzXloUx9o0m8xk/edit?usp=sharing ) 

## Features 🔥

- **Overall Analysis:** Get an overview of your portfolio's performance, including profit/loss and return on investment (ROI).

- **Instrument-Specific Analysis:** Dive into detailed profit/loss and ROI calculations for each individual instrument within your portfolio.

- **Open Positions Details:** View real-time information about open positions, including daily, weekly, and monthly profit/loss calculations.

- **Historical Analysis:** Analyze your portfolio's historical performance using customizable time intervals such as daily and weekly windows.

- **Multi-Currency Support**: Generate reports in various currencies, allowing you to evaluate your portfolio's performance across different monetary units.

- **Flexible Instrument Types**: Analyze portfolios consisting of diverse instrument types, such as stocks, mutual funds, and precious metals.

## Usage

You have to define your transactions in a csv file with following format:

| date (dd/MM/yyyy HH:mm:ss) | instrument type (BIST \| FUND \| CURRENCY) | symbol | transaction type (BUY \| SELL) | amount | price | fee | currency (TRY, USD, EUR) |
|----------------------------|--------------------------------------------|--------|--------------------------------|--------|-------|-----|--------------------------|
| 01/01/2023 10:30:00        | BIST                                       | FENER  | BUY                            | 10     | 45    | 5   | TRY                      |
| 15/01/2023 10:30:00        | BIST                                       | FENER  | BUY                            | 5      | 50    | 5   | TRY                      |


Run to create report with default parameters;
```
java -jar pa.jar -i <csv-file-location>
```

**CLI Parameters**
|  Parameter | Description  |
|---|---|
| -i --input-file (Required)      | Csv file path which transactions defined in                                                                     |
| -d --date                       | Date the report will be generated                                                                               |
| -c --currency                   | Currencies in which the reports will be created (separated by comma)                                            |
| -fi --filtered-instrument-types | Instrument types that will not be included in the calculation when calculating the report (separated by comma)  |
| -fs --filtered-symbols          | Symbols that will not be included in the calculation when calculating the report (separated by comma)           |
| -o --output-file                | Output file path                                                                                                |

## Contributing 💞

We welcome contributions from the community! If you'd like to contribute to Portfolio Analyzer, please follow these steps:

1. Fork the repository.
2. Create a new branch: `git checkout -b feature/your-feature-name`
3. Make your changes and commit them.
4. Push to your forked repository.
5. Create a pull request describing your changes.

## License
[MIT](https://choosealicense.com/licenses/mit/)

## Contact

If you have any questions, suggestions, or feedback, you can reach me at [oztiryakimeric@gmail.com]

---

I hope that the Portfolio Analyzer proves to be a valuable tool for analyzing and managing your financial portfolios. Your feedback and contributions are highly appreciated!
