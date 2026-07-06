# ORDER BOOK MATCHING ENGINE

An exchange allows the buyers and sellers of a product to discover each other and trade. Buyers and
sellers (traders) submit orders to the exchange and the exchange applies simple rules to determine if a
trade has occurred. The dominant kind of exchange is a central limit order book (CLOB) where orders
are matched using ‘price time priority’.
When placing an order, traders specify if they wish to buy or sell, the limit price ie. worst possible price
they will trade at, and the volume (number of shares) they wish to trade. On our exchange trades only
occur during the processing of a newly posted order, and happen immediately, which is known as
‘continuous trading’.

## Matching example

As orders arrive at the exchange, the are considered for aggressive matching first against the opposite
side of the book. Once this completes, any remaining order volume will rest on their own side of the
book. Consider 3 orders have been submitted to the exchange, in the following order:

* Buy 1000 @ 99
* Buy 1200 @ 98
* Buy 500 @ 99

As there are no Sell orders yet, they rest on the order book as follows (note **Buy** for **98** is lowest priority):
<table>
  <tr>
    <th colspan="2">Bids (Taker buying)</th>
    <th colspan="2">Asks (Maker selling)</th>
  </tr>
  <tr><th>Volume</th><th>Price</th><th>Price</th><th>Volume</th></tr>
  <tr><td>1,000</td><td>99</td><td></td><td></td></tr>
  <tr><td>500</td><td>99</td><td></td><td></td></tr>
  <tr><td>1,200</td><td>98</td><td></td><td></td></tr>
</table>

Price time priority refers to the order in which orders in the book are eligible to be matched during the
aggressive phase. Orders are first matched in order of price (most aggressive to least aggressive), then
by arrival time into the book (oldest to newest). A **Sell** order is now submitted, with a limit price that
does not cross with any of the existing resting orders:

* Sell 2000 @ 101

<table>
    <tr>
        <th colspan="2">Bids (Taker buying)</th>
        <th colspan="2">Asks (Maker selling)</th>
    </tr>
    <tr><th>Volume</th><th>Price</th><th>Price</th><th>Volume</th></tr>
    <tr><td>1,000</td><td>99</td><td>101</td><td>2,000</td></tr>
    <tr><td>500</td><td>99</td><td></td><td></td></tr>
    <tr><td>1,200</td><td>98</td><td></td><td></td></tr>
</table>

A **Sell** order is now submitted that is aggressively-priced:

* Sell 2000 @ 95

This triggers a matching event as there are orders on the Buy side that match with the new Sell order.
The orders are matched in price time priority (first by price, then by arrival time into the book) i.e.

* Buy 1000 @ 99 is matched first (as it is the oldest order at the highest price level)
* Buy 500 @ 99 is matched second (as it is at the highest price level and arrived after the BUY 1000 @ 99 order)
* Buy 500 @ 98 is matched third (as it is at a lower price. This partially fills the resting order of 1200, leaving 700
  in the order book)

<table>
    <tr>
        <th colspan="2">Bids (Taker buying)</th>
        <th colspan="2">Asks (Maker selling)</th>
    </tr>
    <tr><th>Volume</th><th>Price</th><th>Price</th><th>Volume</th></tr>
    <tr><td>700</td><td>98</td><td>101</td><td>2,000</td></tr>
</table>

### A: Limit order handling

The sample example is to produce executable code that will accept orders from standard input, and to emit to
standard output the trades as they occur. Once standard input ends, the program should print the final
contents of the order book.
Order inputs will be given as a comma separated values, one order per line of the input, delimited by a
new line character. The fields are: `order-id, side, price, volume`. Side will have a value of ‘B’
for **Buy** or ‘S’ for **Sell**. Price and volume will both be integers. order-id should be handled as a string.

__**Example 1**__

In this example no buyer or seller is willing the pay the opposing price so no trades occur.

```
exchange$ cat test01.txt
    10000,B,98,25500
    10005,S,105,20000
    10001,S,100,500
    10002,S,100,10000
    10003,B,99,50000
    10004,S,103,100
exchange$ engine < test01.txt
        50,000 99   | 100 500
        25,500 98   | 100 10,000
                    | 103 100
                    | 105 20,000
exchange$ _
```

Which represents the following order book:
<table>
    <tr>
        <th colspan="2">Bids (Taker buying)</th>
        <th colspan="2">Asks (Maker selling)</th>
    </tr>
    <tr><th>Volume</th><th>Price</th><th>Price</th><th>Volume</th></tr>
    <tr><td>50,000</td><td>99</td><td>100</td><td>500</td></tr>
    <tr><td>25,500</td><td>98</td><td>100</td><td>10,000</td></tr>
    <tr><td></td><td></td><td>103</td><td>100</td></tr>
    <tr><td></td><td></td><td>105</td><td>20,000</td></tr>
</table>

__**Example 2**__

If an order is then submitted to Buy 16000 @ 105p, it will fill completely against the resting orders,
producing the following output:

```
exchange$ cp test01.txt test02.txt
exchange$ echo "10006,B,105,16000" >> test02.txt
exchange$ engine < test02.txt
    trade 10006,10001,100,500
    trade 10006,10002,100,10000
    trade 10006,10004,103,100
    trade 10006,10005,105,5400
        50,000 99 | 105 14,600
        25,500 98 |
exchange$ _      
```

Trade output must indicate the aggressing `order-id`, the resting `order-id`, the `price` of the match and
the `volume` traded, followed by a newline.

The order book output should be formatted to a fixed width using the following template:

```
Buyers             | Sellers
000,000,000 000000 | 000000 000,000,000
```

Please note that once submitted, orders are not modified by further input. There is no need to maintain
more than one order book, all orders are for the same product.

### A: Iceberg Orders handling

The input format now supports an additional column, `visible-quantity` as the fifth value to indicate an
iceberg order.

The implementation of Iceberg orders should follow the supplemented document '**SETSmm and
Iceberg Orders**' published by the London Stock Exchange sections 4.2 - 4.2.3.2. The relevant
documentation on Iceberg orders provided below for reference.

The output format is unchanged.

```
exchange$ cat test_ice01.txt
    10000,B,98,25500
    10005,S,101,20000
    10002,S,100,10000
    10001,S,100,7500
    10003,B,99,50000
    ice1,B,100,100000,10000
exchange$ engine < test_ice01.txt
    trade ice1,10002,100,10000
    trade ice1,10001,100,7500
    10,000 100 | 101 20,000
        50,000 99 |
        25,500 98 |
exchange$ _
```

The order book display shows the currently visible portion of the iceberg order `ice1`.

***
___

---

## Excerpts from 'SETSmm and Iceberg Orders' published by the London Stock Exchange sections 4.2 - 4.2.3.2.

### 4.2 Iceberg orders

This section sets out the use and behaviour of iceberg orders on the Exchange’s order driven trading services. Iceberg
orders will be available on SETS and the International Order Book (IOB) from 22 September 2003. Iceberg orders will be
available on SETSmm from when this service goes live on 3 November 2003.

#### 4.2.1  Background

Market participants with large orders to execute may be reluctant to expose these to the order book in their entirety
because of the potential adverse market impact of doing so. Investors and brokers consequently adopt alternative trading
strategies, including dealing away from the order book, splitting the order into smaller fragments, or drip-feeding the
order into the order book using automatic entry facilities (often known as “tranching”).
Introducing iceberg orders will facilitate such trading practices by offering the ability to execute such business in
the central order book, thereby strengthening the market by concentrating liquidity.

#### 4.2.2  Benefits of iceberg orders

By its nature iceberg orders allow the originator to only display a smaller part
of a larger order in order to limit the market impact costs of that order. The benefits of iceberg orders over and above
the use of existing automated input facilities (referred to above as ‘tranching’) are as follows:

* An iceberg order will increase the originator’s execution capabilities by maximising volume executed in a single order
  book execution at the same price (tranching in an automated input facility will only result in the ‘peak’ size being
  executed).
* Customers looking to execute aggressively on the order book, eg using an At Best order, are more likely to achieve
  better prices when iceberg orders are available for execution as these will exhaust total iceberg volume before
  executing against orders further down the price queue).

To illustrate the above, consider the following example where a participant has entered an order to sell 100,000 shares
at a limit price of 100p into a tranching facility. The participant has chosen to show the order in tranches of 10,000
shares. The order in bold in the order book below indicates the first portion of the tranche order.
<table>
    <tr>
        <th colspan="2">Bids (Taker buying)</th>
        <th colspan="2">Asks (Maker selling)</th>
    </tr>
    <tr><th>Volume</th><th>Price</th><th>Price</th><th>Volume</th></tr>
    <tr><td>50,000</td><td>99</td><td>100</td><td>500</td></tr>
    <tr><td>25,500</td><td>98</td><td><b>100</b></td><td><b>10,000</b></td></tr>
    <tr><td></td><td></td><td>103</td><td>100</td></tr>
    <tr><td></td><td></td><td>105</td><td>20,000</td></tr>
</table>
Assume now that an at best order to buy 16,000 shares is entered. During execution, the incoming order will fully execute the first portion of the tranche order, and some of the sell orders at worse prices. Only once matching is completed, will the trading system send out execution confirmation messages to affected participants.
Upon receiving a message informing it that the visible tranche has been fully executed, the tranching facility will automatically submit another order to sell 10,000 shares at a limit price of 100. The order book will then appear as follows:
<table>
    <tr>
        <th colspan="2">Bids (Taker buying)</th>
        <th colspan="2">Asks (Maker selling)</th>
    </tr>
    <tr><th>Volume</th><th>Price</th><th>Price</th><th>Volume</th></tr>
    <tr><td>50,000</td><td>99</td><td><span style="color: #FF0000;"><b>100</b></span></td><td><span style="color: #FF0000;"><b>10,000</b></span></td></tr>
    <tr><td>25,500</td><td>98</td><td>105</td><td><b>14,600</b></td></tr>
</table>
* Notes: looks <span style="color: #FF0000;">red</span> is incorrect ( to be verified )

Despite being willing to sell at a strictly better (in this case lower) price than the limit orders at 103p and 105p,
the tranching response mechanism has allowed trading to pass through the limit price of the large order.

Since iceberg orders reside in the Exchange’s system, the hidden volume of an iceberg order is included in the matching
algorithm. This means that unlike the above scenario, trading cannot pass through a price limit of an iceberg order
until all volume (including hidden volume) at that price limit has been satisfied. Specifically, the iceberg order has
brought the following market efficiencies:
The originator of the iceberg order would have had 15,500 executed as opposed to just 10,000

* The originator of the iceberg order would have had 15,500 executed as opposed to just 10,000
* The originator of the At Best order has traded all its volume at 100p at a consideration of £16,000 thanks to the
  iceberg order. This compares to £16,273 if no iceberg was present, a saving of £273 or 1.7% of total consideration.

#### 4.2.3  How iceberg orders work

An iceberg order is an order that can be partially hidden from market view. Upon entry of the order, the participant
must therefore specify the total order size and the visible “peak” size. The peak size is the maximum volume that will
be shown to the market at any given instant. To maintain sufficient transparency in the market, a minimum peak size will
be set, defined as a fraction of NMS for that stock.

The trading system will manage the iceberg order by automatically introducing new full peaks into the matching algorithm
and order book following complete execution of a revealed peak. Each time a new peak enters the order book, it is
assigned a new timestamp and behaves in an identical manner to a conventional limit order. Acquisition of a new time
stamp means that the hidden volume of an iceberg order loses time priority to other (visible) limit orders at the same
price. As described above, however, the total volume (visible and hidden) of an iceberg order retains continuous price
priority over all other volume at a strictly worse price.

In the examples that follow, the peaks of iceberg orders are identified in bold to aid understanding. In practice,
however, the visible peaks of iceberg orders will not be distinguishable from conventional limit orders.

##### 4.2.3.1  Aggressive iceberg order entry

When an iceberg enters an order book aggressively, it will participate with its full volume. Any remaining volume of the
order will then be shown to the market in peaks of a size specified by the participant.

<table>
    <tr>
        <th colspan="3">Bids (Taker buying)</th>
        <th colspan="3">Asks (Maker selling)</th>
    </tr>
    <tr><th>Time</th><th>Volume</th><th>Price</th><th>Price</th><th>Volume</th><th>Time</th></tr>
    <tr><td>8:20:25</td><td>50,000</td><td>99</td><td>100</td><td>10,100</td><td>8:20:32</td></tr>
    <tr><td>8:24:09</td><td>25,500</td><td>98</td><td>100</td><td>7,500</td><td>8:22:57</td></tr>
    <tr><td></td><td></td><td></td><td>101</td><td>20,100</td><td>8:19:00</td></tr>
</table>

For example, an iceberg order to buy 100,000 shares at a price of 100p is entered at 8:25:00 into the above order book,
and the participant has elected to define the peak size as 10,000 shares.

The iceberg order will enter the order book aggressively, immediately matching against the two sell orders at 100p.
These trades are effected, and the remaining iceberg size is 82,500 shares. The first peak of 10,000 shares is then
entered into the order book appearing as a conventional limit order. The order book will appear as below.
<table>
    <tr>
        <th colspan="3">Bids (Taker buying)</th>
        <th colspan="3">Asks (Maker selling)</th>
    </tr>
    <tr><th>Time</th><th>Volume</th><th>Price</th><th>Price</th><th>Volume</th><th>Time</th></tr>
    <tr><td><b>8:25:00</b></td><td><b>10,000</b></td><td><b>100</b></td><td>101</td><td>20,000</td><td>8:19:00</td></tr>
    <tr><td>8:20:25</td><td>50,000</td><td>99</td><td></td><td></td><td></td></tr>
    <tr><td>8:24:09</td><td>25,500</td><td>98</td><td></td><td></td><td></td></tr>
</table>

##### 4.2.3.2  Passive iceberg order execution

Suppose now that an order to sell 10,000 shares At Best is entered at 8:25:32 in our previous example.

This incoming order will fully execute the visible peak of the iceberg. Once matching has occurred, the trading system
will automatically refresh the peak in the order book, assigning it a new time stamp. Total remaining iceberg volume is
then 72,500 shares – 10,000 of which are visible in the order book as below.
<table>
    <tr>
        <th colspan="3">Bids (Taker buying)</th>
        <th colspan="3">Asks (Maker selling)</th>
    </tr>
    <tr><th>Time</th><th>Volume</th><th>Price</th><th>Price</th><th>Volume</th><th>Time</th></tr>
    <tr><td><b>8:25:32</b></td><td><b>10,000</b></td><td><b>100</b></td><td>101</td><td>20,000</td><td>8:19:00</td></tr>
    <tr><td>8:20:25</td><td>50,000</td><td>99</td><td></td><td></td><td></td></tr>
    <tr><td>8:24:09</td><td>25,500</td><td>98</td><td></td><td></td><td></td></tr>
</table>
Multiple executions of an iceberg order on the order book will only generate a single trade message (5TG) for the iceberg participant (ie when an incoming order executes against the peak of an iceberg order and some or all of the hidden volume).

To show this, consider an order to sell 11,000 At Best which enters the above order book at 8:26:12. Since trading
cannot pass through the price limit of 100p whilst there is still some volume of the iceberg to be satisfied, the
incoming order will match against the revealed peak of the iceberg order, and 1,000 shares of the hidden volume. The
trading system will disseminate these trades in a single message (5TG), listing the separate trade codes.

The total volume of our iceberg order is now 61,500 shares, and the trading system must update the order book with the
new peak of the iceberg. In the above example, the new peak will be 9,000 shares (peak size minus the 1,000 shares
already matched of this peak) and the partially executed peak will retain price priority. It will only be refreshed when
a subsequent execution takes the remaining peak size. The execution will refresh the peak and generate a new time
priority. The order book will therefore appear as below.
<table>
    <tr>
        <th colspan="3">Bids (Taker buying)</th>
        <th colspan="3">Asks (Maker selling)</th>
    </tr>
    <tr><th>Time</th><th>Volume</th><th>Price</th><th>Price</th><th>Volume</th><th>Time</th></tr>
    <tr><td><b>8:26:12</b></td><td><b>9,000</b></td><td><b>100</b></td><td>101</td><td>20,000</td><td>8:19:00</td></tr>
    <tr><td>8:20:25</td><td>50,000</td><td>99</td><td></td><td></td><td></td></tr>
    <tr><td>8:24:09</td><td>25,500</td><td>98</td><td></td><td></td><td></td></tr>
</table>
Suppose that a second iceberg order is entered at 8:28:00 to buy 50,000 shares at a limit price of 100p, with a revealed peak size of 20,000 shares. To distinguish the iceberg orders, they have been labeled A and B. The first peak of the new iceberg (B) will be entered into the order book, which will now appear as below.
<table>
    <tr>
        <th colspan="3">Bids (Taker buying)</th>
        <th colspan="3">Asks (Maker selling)</th>
    </tr>
    <tr><th>Time</th><th>Volume</th><th>Price</th><th>Price</th><th>Volume</th><th>Time</th></tr>
    <tr><td><b>8:26:12</b></td><td><b>9,000<sup>A</sup></b></td><td><b>100</b></td><td>101</td><td>20,000</td><td>8:19:00</td></tr>
    <tr><td><b>8:28:00</b></td><td><b>20,000<sup>B</sup></b></td><td><b>100</b></td><td></td><td></td><td></td></tr>
    <tr><td>8:20:25</td><td>50,000</td><td>99</td><td></td><td></td><td></td></tr>
</table>
When there are multiple icebergs at a single price level, then the new peaks of the iceberg orders retain time priority amongst themselves. For example, if an order to sell 35,000 shares At Best is now entered at 8:30, then the visible peaks of both icebergs will be completely filled, and iceberg A will satisfy the remaining 6,000 shares of the incoming order.

The market will see two trade reports from this series of executions – one for 15,000 shares (against iceberg A), and
one for 20,000 shares (against iceberg B). Once matching has been completed, new peaks of both icebergs are introduced
to the order book, with iceberg A retaining priority over iceberg B.

<table>
    <tr>
        <th colspan="3">Bids (Taker buying)</th>
        <th colspan="3">Asks (Maker selling)</th>
    </tr>
    <tr><th>Time</th><th>Volume</th><th>Price</th><th>Price</th><th>Volume</th><th>Time</th></tr>
    <tr><td><b>8:30:00</b></td><td><b>4,000<sup>A</sup></b></td><td><b>100</b></td><td>101</td><td>20,000</td><td>8:19:00</td></tr>
    <tr><td><b>8:30:00</b></td><td><b>20,000<sup>B</sup></b></td><td><b>100</b></td><td></td><td></td><td></td></tr>
    <tr><td>8:20:25</td><td>50,000</td><td>99</td><td></td><td></td><td></td></tr>
</table>

##### 4.2.3.3  Iceberg order participation in auctions

Iceberg orders participate in auctions, contributing their full volume to the matching algorithm. The volume visible to
the market, however, will continue to be the visible peak volume. The indicative uncrossing volume message, however,
will detail total auction volume including that which is matched against hidden volume of one of more iceberg orders.

##### 4.2.3.3  Iceberg order modification

Iceberg orders are eligible for order modification by order price, order size, participant order reference, date and
time

__**<u>Order Price</u>**__

If the order price is changed the order will lose time priority.

__**<u>Order Size</u>**__
The order size can be changed in several ways

* If the order size is increased, the order will maintain time priority, the peak size will remain unchanged, and the
  hidden size will increase
* If the order size is decreased, and the resulting remaining total size is greater or equal to the current remaining
  peak size, the order will maintain time priority, the peak size will remain unchanged, and the hidden size will be
  reduced
* If the order size is decreased, and the resulting remaining total size is less than the current remaining peak size,
  the order will lose time priority and be refreshed with a remaining peak size equal to the remaining total size

Please note that it is not possible to modify the peak refresh size of the iceberg order (note, however, that the
remaining peak size will be reduced if the total iceberg size is modified to a size less than the currently displayed
peak).

__**<u>Participant order reference, date and time</u>**__

The participant order reference, date and time can only be modified if the price and/or volume is also changed. As this
is a change to the visible order it will lose time priority.

##### 4.2.3.3  Iceberg order management

The owner of the iceberg order will receive a “hidden” order code. This code refers to the order as a whole, and can be
used to delete the remaining (total) iceberg volume. If the iceberg order is modified resulting in the displayed size
changing, a new hidden order code will be generated.
Each new peak of the iceberg order will also be assigned a unique “visible” order code, seen by the market. Since this
code is unique and varies with each new peak, market participants will not be able to tell that the successive peaks are
part of the same iceberg order.
The owner of the order is the only participant knowing both the hidden and visible order codes, allowing them to retain
full “view and do” capability.


## Runtime options and testing

The engine supports a small runtime option to enable LSE-style aggregated trade dissemination (single-line 5TG messages) for resting participants that are hit multiple times during processing of a single incoming order.

- To enable aggregation set the system property `lse.aggregate=true` when running the JVM. Example:

```bash
java -Dlse.aggregate=true -jar dsucs-exhange-iceberg-1.0-SNAPSHOT.jar < test02.txt
```

When enabled the compact trades output will emit lines like:

```
5TG <restingId>: <buyId>,<sellId>,<price>,<qty>; <buyId>,<sellId>,<price>,<qty>
```

The default behavior is unchanged (one `trade` line per match). Unit tests in `src/test/java/org/dsucs/engine` cover both modes.

The compact `printOrders` layout aims to match the README examples. Tests assert the header `Buyers             | Sellers` and that rows align with the numeric layout shown in examples.


