//
//  ViewController.swift
//  LightningHome
//
//  Created by Pallavi Gudipati on 23/12/16.
//  Copyright © 2016 Pallavi Gudipati. All rights reserved.
//

import UIKit

class ViewController: UIViewController {

    @IBOutlet weak var infoText: UITextView!
    @IBOutlet weak var canTalk: UISwitch!
    @IBOutlet weak var textView: UITextView!
    var projectConfig = Config()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view, typically from a nib.
        canTalk.setOn(false, animated: false)
        textView.text = "START"
    }

    @IBAction func canTalkValueChange(_ sender: Any) {
        if canTalk.isOn {
            var request = URLRequest(url: projectConfig.speechServer)
            request.httpMethod = "GET"
            let session = URLSession(configuration: URLSessionConfiguration.default)
            print("THIS LINE IS PRINTED")
            let task = session.dataTask(with: request, completionHandler: {(data, response, error) -> Void in
                if let data = data {
                    let strResponse = NSString(data: data, encoding: String.Encoding.utf8.rawValue)
                    // print("Response String :\(strResponse)")
                    DispatchQueue.main.async {
                        // update some UI
                        self.textView.text = strResponse as! String
                    }
                } else {
                    print("Something fishy")
                }
            })
            task.resume()

            // textView.text = "ON"
        } else {
            textView.text = "OFF"
        }
    }
    
    
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

}

