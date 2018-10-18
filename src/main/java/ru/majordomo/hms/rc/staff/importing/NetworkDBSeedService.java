package ru.majordomo.hms.rc.staff.importing;

import com.google.common.collect.ImmutableSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Set;

import ru.majordomo.hms.rc.staff.repositories.NetworkRepository;
import ru.majordomo.hms.rc.staff.resources.Network;

@Service
@Profile("import")
public class NetworkDBSeedService {
    private final static Logger logger = LoggerFactory.getLogger(NetworkDBSeedService.class);

    private NetworkRepository networkRepository;

    @Autowired
    public NetworkDBSeedService(NetworkRepository networkRepository) {
        this.networkRepository = networkRepository;
    }

    public boolean seedDB() {
        networkRepository.deleteAll();

        this.seed();

        return true;
    }

    private void seed() {
        Set<String> networks = ImmutableSet.<String>builder()
                .add("lo|127.0.0.0|8|127.0.0.1|0")
                .add("default|0.0.0.0|0|0.0.0.1|1")
                .add("78.108.80.0/24 mr|78.108.80.0|24|78.108.80.254|80")
                .add("78.108.81.0/24 mr|78.108.81.0|24|78.108.81.254|16")
                .add("78.108.82.0/23 mr|78.108.82.0|23|78.108.83.254|82")
                .add("78.108.84.0/23 mr|78.108.84.0|23|78.108.85.254|16")
                .add("78.108.86.0/23 mr|78.108.86.0|23|78.108.87.254|16")
                .add("178.250.244.0/23 mr|178.250.244.0|23|178.250.245.254|244")
                .add("178.250.246.0/23 mr|178.250.246.0|23|178.250.247.254|246")
                .add("185.84.108.0/23 mr|185.84.108.0|23|185.84.109.250|109")
                .add("185.84.110.0/23 mr|185.84.110.0|23|185.84.111.254|111")
                .add("172.16.103.0/24 mr|172.16.103.0|24|172.16.103.1|253")
                .add("192.168.254.0/24 mr|192.168.254.0|24|192.168.254.1|254")
                .add("78.108.88.0/23 dh|78.108.88.0|23|78.108.89.254|17")
                .add("78.108.90.0/23 dh|78.108.90.0|23|78.108.91.254|17")
                .add("78.108.92.0/23 dh|78.108.92.0|23|78.108.93.254|17")
                .add("78.108.94.0/23 dh|78.108.94.0|23|78.108.95.254|17")
                .add("178.250.240.0/23 dh|178.250.240.0|23|178.250.241.254|17")
                .add("178.250.242.0/23 dh|178.250.242.0|23|178.250.243.254|17")
                .add("172.16.102.0/24|172.16.102.0|24|172.16.102.1|253")
                .build();

        networks.forEach(s -> {
            String[] networkData = s.split("\\|");
            Network network = new Network();
            network.setSwitchedOn(true);
            network.setName(networkData[0]);
            network.setAddress(networkData[1]);
            network.setMask(Integer.valueOf(networkData[2]));
            network.setGatewayAddress(networkData[3]);
            network.setVlanNumber(Integer.valueOf(networkData[4]));

            networkRepository.save(network);

            logger.debug(network.toString());
        });
    }
}
