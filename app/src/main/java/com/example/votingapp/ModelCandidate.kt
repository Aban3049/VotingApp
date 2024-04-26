package com.example.votingapp

class ModelCandidate {

    var name:String = ""
    var partyName:String = ""
    var profileImageURl:String = ""
    var timestamp:Long = 0
    var id:String = ""
    var voteCount:Long = 0

    constructor()

    constructor(name: String, partyName: String, profileImageURl: String, timestamp: Long, id:String,voteCount: Long) {
        this.name = name
        this.partyName = partyName
        this.profileImageURl = profileImageURl
        this.timestamp = timestamp
        this.id = id
        this.voteCount = voteCount
    }
}