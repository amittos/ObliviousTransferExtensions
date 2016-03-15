# Oblivious Transfer Extension

## Summary

This is a concrete implementation of the [IKPN03](https://www.iacr.org/cryptodb/data/paper.php?pubkey=1432) protocol written in Java, using the [SCAPI](http://scapi.readthedocs.org/en/latest/intro.html) interface. 
This project was made for my MSc thesis for the department of [Information & Communication Systems Engineering](http://msc.icsd.aegean.gr/) of the [University of the Aegean](http://www.aegean.gr/).
This project is distributed under the [GNU AFFERO GENERAL PUBLIC LICENSE](http://www.gnu.org/licenses/agpl-3.0.txt). 

## Installation instructions 

To run this project you'll need to have installed: 

* Java 8 or greater
* The [SCAPI](http://scapi.readthedocs.org/en/latest/install.html) library

## Running Instructions

If the Sender and the Receiver are using the same address, simply run the `SenderMain` and the `ReceiverMain` classes. If the the parties are using different addresses, you'll need to modify the `SocketParties.properties` file accordingly. More information [here](http://scapi.readthedocs.org/en/latest/communication.html#setting-up-communication). 

To configure the number of transactions, simply edit the `m` value in both the `PSender` and `PReceiver` classes in the default constructor method. Bare in mind though that the `m` value **MUST** be divisible by 8 and of course it must be the same in both classes. 

## Where to get help

If you experience problems with SCAPI you can use the following links: 

* SCAPI's GitHub project: https://github.com/cryptobiu/scapi
* SCAPI's Documentation: http://scapi.readthedocs.org/
* SCAPI's API: http://cryptobiu.github.io/scapi/

If you have any inquiries, feel free to email me at: *a.mittos [at] outlook [dot] com*. 

## Contribution guidelines

I'm planning to continue developing this project. My to-do list is the following: 

* Multithreading support
* Support against malicious adversaries (see [here](https://eprint.iacr.org/2015/061))
* 1-out-of-N support (see [here](https://eprint.iacr.org/2013/491))
* Implementation of Tung Chou and Claudio Orlandi's OT protocol of the OT part (see [here](https://eprint.iacr.org/2015/267)) 

If you want to contribute to this project, you can implement any of the above bullets or implement something entirely different. Of course, bug fixing and implementation optimizations are always welcome.

## More on the protocol

Paper Title: Extending Oblivious Transfers Efficiently<br>
Authors: Yuval Ishai, Joe Kilian, Kobbi Nissim, Erez Petrank<br>
Booktitle: Advances in Cryptology - CRYPTO 2003, 23rd Annual International Cryptology Conference, Santa Barbara, California, USA, August 17-21, 2003, Proceedings<br>
Year: 2003<br>
Link: https://www.iacr.org/cryptodb/data/paper.php?pubkey=1432

## Licence 

This project is distributed under the [GNU AFFERO GENERAL PUBLIC LICENSE](http://www.gnu.org/licenses/agpl-3.0.txt). 
